
import java.util.*;
import java.io.*;
import java.util.logging.Logger;

/**
Instances of this class represent a COBOL program.  One or more
COBOL programs may be present in each COBOL source file.  COBOL
programs may be nested within other COBOL programs.

<p>An attempt is made to resolve dynamic CALLs of the CALL identifier
(or data-name, if you prefer) form.  This entails tracking down
references to the identifier in MOVE an SET statements in addition
to the original VALUE clause, should there be one.

<p>Complicating this is the possibility that the identifier may be
marked GLOBAL or EXTERNAL and thus may not be contained in this
program at all, but in a program in which this program has been
nested.
*/

class CobolProgram {

	private String myName = this.getClass().getName();
	private UUID uuid = UUID.randomUUID();
	private Logger LOGGER = null;
	private CobolSource cobolSource = null;
	private String programName = null;
	private ArrayList<CallWrapper> calledNodes = new ArrayList<>();
	private ArrayList<AssignClause> assignClauses = new ArrayList<>();
	public ArrayList<DDNode> dataNodes = new ArrayList<>(); //TODO make private
	public ArrayList<CobolProgram> programs = new ArrayList<>(); //TODO make private
	private ArrayList<MoveStatement> moves = new ArrayList<>();
	private ArrayList<Identifier> sets = new ArrayList<>();
	private CobolProgram parent = null;
	private int conditionalStatementCount = 0;
	private int statementCount = 0;

	public CobolProgram(
			String programName
			, Logger LOGGER
			) {
		this.programName = programName;
		this.LOGGER = LOGGER;
	}

	public Boolean hasThisProgramName(String programName) {
		return this.programName.equals(programName);
	}

	public Boolean hasThisProgramName(CobolProgram pgm) {
		return this.hasThisProgramName(pgm.getProgramName());
	}

	public Boolean hasThisDDNode01(DDNode ddNode) { //TODO remove
		for (DDNode dataNode: this.dataNodes) {
			if (dataNode.getLevel() != 1) continue;
			if (dataNode.getUUID() == ddNode.getUUID()) {
				return true;
			}
		}

		return false;
	}

	/**
	For each CALL, if it is of the form CALL identifier, locate the
	identifier in the list of DDNodes for this program and any programs
	in which this program is nested.

	<p>Once the DDNode has been found, locate any MOVE or SET statements
	that alter its contents.

	<p>Finally, instruct any nested programs to do the same.
	*/
	public void resolveCalledNodes() {
		LOGGER.fine(this.myName + " " + this.getProgramName() + " resolveCalledNodes");

		for (CallWrapper call: this.calledNodes) {
			LOGGER.finest("  call.identifier = " + call.getIdentifier());
			if (call.getIdentifier() == null) {
			} else {
				Boolean resolved = false;
				CobolProgram pgm = this;
				while (pgm != null && !resolved) {
					resolved = this.resolveCalledNode(call, pgm);
					pgm = pgm.getParent();
					LOGGER.finest(" parent = " + pgm);
				}
				if (resolved) {
					this.findAllTheRightMoves(call);
				} else {
					this.LOGGER.warning(
						"identifier " 
						+ call.getIdentifier()
						+ " not found in " 
						+ call.getCallingModuleName()
						+ " or any program in which it is nested");
				}
				if (call.getDataNode() != null) {
					this.findAllTheRightSets(call);
				}
			}
		}

		for (CobolProgram pgm: this.programs) {
			pgm.resolveCalledNodes();
		}
	}

	/**
	Search the <code>DDNodes</code> in the passed program for the one that matches the
	identifier in the passed <code>CallWrapper</code>.
	*/
	private Boolean resolveCalledNode(CallWrapper call, CobolProgram pgm) {
		this.LOGGER.fine(this.myName + " resolveCalledNode(" + call + ", " + pgm + ")");
		Boolean rc = false;
		ArrayList<DDNode> calledDataNodes = new ArrayList<>();
		ArrayList<DDNode> localDataNodes = null;
		if (pgm == this) {
			localDataNodes = this.getDataNodes();
		} else {
			localDataNodes = pgm.getPublicDataNodes();
		}
		for (DDNode node: localDataNodes) {
			if (node.getParent() == null) {
				calledDataNodes.addAll(node.findChildrenNamed(call.getIdentifier()));
			}
		}
		LOGGER.finest("  all node children named " + call.getIdentifier() + " = " + calledDataNodes);
		rc = call.selectDataNode(calledDataNodes);
		LOGGER.finest("call.dataNode = " + call.getDataNode());
		return rc;
	}

	/**
	Find MOVE statements that seem to reference the identifier in
	the passed CALL statement.

	<code>
	[...]
	       01  CALLED-PROGRAMS.
	           05  PGM1 PIC X(008) VALUE 'CRICHTON'.
	               88  PGM-ZHAAN   VALUE 'ZHAAN'.
	               88  PGM-CHIANA  VALUE 'CHIANA'.
	               88  PGM-DARGO   VALUE 'DARGO'.
	[...]
	           CALL PGM1
	           MOVE 'AERYN' TO PGM1
	           CALL PGM1
	</code>
	*/
	private void findAllTheRightMoves(CallWrapper call) {
		CobolProgram pgm = this;
		while (pgm != null) {
			for (MoveStatement move: pgm.getMoves()) {
				for (Identifier identifier: move.getIdentifiers()) {
					if (identifier.seemsToMatch(call.getId())) {
						call.addCalledModuleName(move.getText());
					}
				}
			}
			pgm = pgm.getParent();
		}

	}

	/**
	Find SET statements that seem to reference the identifier in
	the passed CALL statement.

	<code>
	[...]
	       01  CALLED-PROGRAMS.
	           05  PGM1 PIC X(008) VALUE 'CRICHTON'.
	               88  PGM-ZHAAN   VALUE 'ZHAAN'.
	               88  PGM-CHIANA  VALUE 'CHIANA'.
	               88  PGM-DARGO   VALUE 'DARGO'.
	[...]
	           SET PGM-CHIANA TO TRUE
	           CALL PGM1
	           SET PGM-DARGO TO TRUE
	           CALL PGM1
	</code>
	*/
	private void findAllTheRightSets(CallWrapper call) {
		for (DDNode ee: call.getDataNode().getChildren()) {
			if (!ee.isCondition()) continue;
			this.LOGGER.finest("    call.eightyEight = " + ee);
			for (Identifier identifier: this.sets) {
				this.LOGGER.finest("    identifier.getDataNameText() = " + identifier.getDataNameText());
				if (ee.getIdentifier().equals(identifier.getDataNameText())) {
					this.LOGGER.finest( "    ee.name.equals(ctx...IDENTIFIER())");
					call.addCalledModuleName(ee.getValueInValueClause());
				}
			}
		}
	}

	public UUID getUUID() {
		return this.uuid;
	}

	public String getProgramName() {
		return this.programName;
	}

	public ArrayList<MoveStatement> getMoves() {
		return this.moves;
	}

	public ArrayList<CallWrapper> getCalledNodes() {
		return this.calledNodes;
	}

	public ArrayList<DDNode> getDataNodes() {
		return this.dataNodes;
	}

	/**
	Return DDNodes marked GLOBAL or EXTERNAL as these may be
	referenced in CALL statements by nested programs.
	*/
	public ArrayList<DDNode> getPublicDataNodes() {
		ArrayList<DDNode> publicDataNodes = new ArrayList<>();
		for (DDNode dataNode: this.getDataNodes()) {
			if (dataNode.isGlobal() || dataNode.isExternal()) {
				publicDataNodes.add(dataNode);
			}
		}

		return publicDataNodes;
	}

	public void addProgram(CobolProgram pgm) {
		this.programs.add(pgm);
	}

	public void setParent(CobolProgram parent) {
		this.parent = parent;
	}

	public CobolProgram getParent() {
		return this.parent;
	}

	/**
	Return the nested program with the indicated name.  It is possible that
	the nested program is nested inside a program nested inside this one.

	<p>This is used in the <code>DataDescriptionEntryListener</code> to
	establish the owning program for each <code>DDNode</code>.
	*/
	public CobolProgram nestedProgramNamed(String nestedProgramName) {
		CobolProgram pgm = null;
		for (CobolProgram nestedProgram: this.programs) {
			if (nestedProgram.hasThisProgramName(nestedProgramName)) return nestedProgram;
			pgm = nestedProgram.nestedProgramNamed(nestedProgramName);
			if (pgm != null) break;
		}
		return pgm;
	}

	public void addCall(CallWrapper aCall) {
		Boolean found = false;
		for (CallWrapper cw: this.calledNodes) {
			if (cw.seemsLike(aCall)) {
				found = true;
				break;
			}
		}
		if (!found) this.calledNodes.add(aCall);
	}

	public void addDDNode(DDNode node) {
		this.dataNodes.add(node);
	}

	public void addAssignClause(AssignClause assignClause) {
		this.assignClauses.add(assignClause);
	}

	public void addMoveStatement(MoveStatement move) {
		this.moves.add(move);
	}

	public void addSetTo(Identifier identifier) {
		this.sets.add(identifier);
	}

	/**
	Increment counters for different types of statements that may be
	of interest.  This could grow over time, for example CICS or SQL
	statements could be counted here.
	*/
	public void incrementStatementCounter(CobolParser.StatementContext ctx) {
		this.statementCount++;

		if (ctx.ifStatement() != null) {
			CobolParser.IfStatementContext ifStmt = ctx.ifStatement();
			this.conditionalStatementCount++;
			if (ifStmt.ifElse() != null) {
				this.conditionalStatementCount++;
			}
		}

		if (ctx.evaluateStatement() != null) {
			CobolParser.EvaluateStatementContext evalStmt = ctx.evaluateStatement();
			if (evalStmt.evaluateWhenPhrase() != null) {
				this.conditionalStatementCount += evalStmt.evaluateWhenPhrase().size();
			}
			if (evalStmt.evaluateWhenOther() != null) {
				this.conditionalStatementCount++;
			}
		}
	}

	/**
	Write data of interest on the passed PrintWriter.  Instruct nested programs
	to do the same.
	*/
	public void writeOn(PrintWriter out, UUID parentUUID) throws IOException {
		out.printf(
			"PGM,%s,%s,%s,%d,%d\n"
			, this.getUUID().toString()
			, parentUUID.toString()
			, this.programName
			, this.statementCount
			, this.conditionalStatementCount);

		for (CobolProgram pgm: this.programs) {
			pgm.writeOn(out, this.getUUID());
		}

		for (CallWrapper cw: this.calledNodes) {
			cw.writeOn(out, this.getUUID());
		}

		for (AssignClause ac: this.assignClauses) {
			ac.writeOn(out, this.getUUID());
		}
	}

	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(this.programName);
		if (this.programs.size() > 0) {
			sb.append(" nested programs: ");
			sb.append(this.programs);
		}
		return sb.toString();
	}

}