
import java.util.*;
import java.util.logging.*;
import org.antlr.v4.runtime.tree.*;

/**
Instances of this class represent a DD statement.

*/
public class PPDdStatement {

	private Logger LOGGER = null;
	private TheCLI CLI = null;
	private UUID uuid = UUID.randomUUID();
	private String myName = null;
	private String ddName = null;
	private String procName = null;
	private String fileName = null;
	private Boolean inProc = null;
	private JCLPPParser.DdStatementContext ddStmtCtx = null;
	private JCLPPParser.DdStatementConcatenationContext ddStmtConcatCtx = null;
	private List<JCLPPParser.DdParmASTERISK_DATAContext> ddSplatCtx = null;
	private ArrayList<String> blankParms = new ArrayList<>();
	private Hashtable<String, PPKeywordOrSymbolicWrapper> kosParms = new Hashtable<>();
	private Hashtable<String, PPSingleOrMultipleValueWrapper> somvParms = new Hashtable<>();
	private ArrayList<PPSymbolic> symbolics = new ArrayList<>();

	public static ArrayList<PPDdStatement> bunchOfThese(
			JCLPPParser.DdStatementAmalgamationContext ddStmtAmlgnCtx
			, String procName
			, String ddName
			, String fileName
			, Logger LOGGER
			, TheCLI CLI
			) {
		ArrayList<PPDdStatement> dds = new ArrayList<>();

		if (ddStmtAmlgnCtx.ddStatement() == null) {
		} else {
			dds.add(new PPDdStatement(ddStmtAmlgnCtx.ddStatement(), procName, ddName, fileName, LOGGER, CLI));
		}

		if (ddStmtAmlgnCtx.ddStatementConcatenation() == null) {
		} else {
			for (JCLPPParser.DdStatementConcatenationContext ddcCtx: ddStmtAmlgnCtx.ddStatementConcatenation()) {
				dds.add(new PPDdStatement(ddcCtx, procName, ddName, fileName, LOGGER, CLI));
			}
		}

		return dds;
	}

	public PPDdStatement(
			JCLPPParser.DdStatementContext ddStmtCtx
			, String procName
			, String ddName
			, String fileName
			, Logger LOGGER
			, TheCLI CLI
			) {
		this.ddStmtCtx = ddStmtCtx;
		this.ddSplatCtx = ddStmtCtx.ddParmASTERISK_DATA();
		this.initialize(procName, ddName, fileName, LOGGER, CLI);
		this.initializeTediously(this.ddStmtCtx.ddParameter());
	}

	public PPDdStatement(
			JCLPPParser.DdStatementConcatenationContext ddStmtConcatCtx
			, String procName
			, String ddName
			, String fileName
			, Logger LOGGER
			, TheCLI CLI
			) {
		this.ddStmtConcatCtx = ddStmtConcatCtx;
		this.ddSplatCtx = ddStmtConcatCtx.ddParmASTERISK_DATA();
		this.initialize(procName, ddName, fileName, LOGGER, CLI);
		this.initializeTediously(this.ddStmtConcatCtx.ddParameter());
	}

	private void initialize(
			String procName
			, String ddName
			, String fileName
			, Logger LOGGER
			, TheCLI CLI
			) {
		this.myName = this.getClass().getName();
		this.procName = procName;
		this.inProc = !(procName == null);
		this.fileName = fileName;
		this.ddName = ddName;
		this.LOGGER = LOGGER;
		this.CLI = CLI;
	}

	private void initializeTediously(List<JCLPPParser.DdParameterContext> ddParms) {
		/*
			The following bad idea is brought to you by the dozens of parameters of
			the DD statement.
		*/
		for (JCLPPParser.DdParameterContext ddParm: ddParms) {
			if (ddParm.ddParmASTERISK() != null) {
				this.blankParms.add("ASTERISK");
				continue;
			}

			if (ddParm.ddParmDATA() != null) {
				this.blankParms.add("DATA");
				continue;
			}

			if (ddParm.ddParmDLM() != null) {
				PPKeywordOrSymbolicWrapper kosw = new PPKeywordOrSymbolicWrapper(ddParm.ddParmDLM().keywordOrSymbolic(), this.procName, this.LOGGER, this.CLI);
				this.kosParms.put("DLM", kosw);
				continue;
			}

			if (ddParm.ddParmSYMBOLS() != null) {
				PPSingleOrMultipleValueWrapper somvw = new PPSingleOrMultipleValueWrapper(ddParm.ddParmSYMBOLS().singleOrMultipleValue(), this.procName, this.LOGGER, this.CLI);
				this.somvParms.put("SYMBOLS", somvw);
				continue;
			}

			if (ddParm.ddParmSYMLIST() != null) {
				PPSingleOrMultipleValueWrapper somvw = new PPSingleOrMultipleValueWrapper(ddParm.ddParmSYMLIST().singleOrMultipleValue(), this.procName, this.LOGGER, this.CLI);
				this.somvParms.put("SYMLIST", somvw);
				continue;
			}

			if (ddParm.SYMBOLIC() != null) {
				this.symbolics = PPSymbolic.bunchOfThese(ddParm.SYMBOLIC(), this.fileName, this.procName, this.LOGGER, this.CLI);
			}
		}
	}

	public void resolveParms(ArrayList<PPSetSymbolValue> sets) {
		this.LOGGER.finest(this.myName + " resolveParms sets = |" + sets + "|");

		for (PPKeywordOrSymbolicWrapper kos: this.kosParms.values()) {
			kos.resolveParms(sets);
		}

		for (PPSingleOrMultipleValueWrapper somv: this.somvParms.values()) {
			somv.resolveParms(sets);
		}

		for (PPSymbolic s: this.symbolics) {
			s.resolve(sets);
		}
	}

	public ArrayList<PPSymbolic> collectSymbolics() {
		this.LOGGER.finer(this.myName + " collectSymbolics");

		ArrayList<PPSymbolic> symbolics = new ArrayList<>();

		for (PPKeywordOrSymbolicWrapper k: kosParms.values()) {
			symbolics.addAll(k.collectSymbolics());
		}

		for (PPSingleOrMultipleValueWrapper s: somvParms.values()) {
			symbolics.addAll(s.collectSymbolics());
		}

		symbolics.addAll(this.symbolics);

		return symbolics;
	}

	public String getResolvedValue(String key) {
		if (kosParms.containsKey(key)) {
			return kosParms.get(key).getResolvedValue();
		} else if (somvParms.containsKey(key)) {
			return somvParms.get(key).getResolvedValue();
		} else {
			return key + " not found";
		}
	}

}
