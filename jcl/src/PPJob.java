
import java.util.*;
import java.util.logging.*;
import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.*;
import org.antlr.v4.runtime.tree.*;

/**


*/
public class PPJob {

	private Logger LOGGER = null;
	private TheCLI CLI = null;
	private UUID uuid = UUID.randomUUID();
	private String myName = null;
	private JCLPPParser.JobCardContext jobCardCtx = null;
	private JCLPPParser.JcllibStatementContext jcllibCtx = null;
	private ArrayList<PPKeywordOrSymbolicWrapper> jcllib = new ArrayList<>();
	private ArrayList<PPProc> procs  = new ArrayList<>();
	private ArrayList<PPSetSymbolValue> symbolics = new ArrayList<>();
	private ArrayList<PPIncludeStatement> includes = new ArrayList<>();
	private ArrayList<PPJclStep> steps = new ArrayList<>();
	private String fileName = null;
	private String jobName = null;
	private int startLine = -1;
	private int endLine = -1;

	public PPJob(JCLPPParser.JobCardContext ctx, String fileName, Logger LOGGER, TheCLI CLI) {
		this.jobCardCtx = ctx;
		this.fileName = fileName;
		this.LOGGER = LOGGER;
		this.CLI = CLI;
		this.initialize();
		LOGGER.finer(this.myName + " " + this.jobName + " instantiated from " + this.fileName);
	}

	private void initialize() {
		myName = this.getClass().getName();
		this.startLine = this.jobCardCtx.JOB().getSymbol().getLine();
		this.jobName = this.jobCardCtx.jobName().NAME_FIELD().getSymbol().getText();
	}

	public void addJcllib(JCLPPParser.JcllibStatementContext ctx) {
		LOGGER.finest(this.myName + " addJcllib: " + this.jobCardCtx.jobName().NAME_FIELD().getSymbol().getText());
		List<JCLPPParser.KeywordOrSymbolicContext> kywdCtxList = ctx.singleOrMultipleValue().keywordOrSymbolic();
		LOGGER.finest(this.myName + " addJcllib ctx.singleOrMultipleValue().keywordOrSymbolic(): " + ctx.singleOrMultipleValue().keywordOrSymbolic());

		this.jcllibCtx = ctx;
		if (kywdCtxList == null || kywdCtxList.size() == 0) {
			kywdCtxList = ctx.singleOrMultipleValue().parenList().keywordOrSymbolic();
			LOGGER.finest(this.myName + " addJcllib ctx.singleOrMultipleValue().parenList().keywordOrSymbolic(): " + ctx.singleOrMultipleValue().parenList().keywordOrSymbolic());
		}

		for (JCLPPParser.KeywordOrSymbolicContext k: kywdCtxList) {
			LOGGER.finest(this.myName + " addJcllib kywdCtxList k: " + k);
			LOGGER.finest(this.myName + " addJcllib kywdCtxList k.KEYWORD_VALUE(): " + k.KEYWORD_VALUE());
			LOGGER.finest(this.myName + " addJcllib kywdCtxList k.SYMBOLIC(): " + k.SYMBOLIC());
			LOGGER.finest(this.myName + " addJcllib kywdCtxList k.QUOTED_STRING_FRAGMENT(): " + k.QUOTED_STRING_FRAGMENT());
			for (TerminalNode t: k.KEYWORD_VALUE()) {
				LOGGER.finest(this.myName + " addJcllib kywdCtxList KEYWORD_VALUE() t.getSymbol().getText(): " + t.getSymbol().getText());
			}
			for (TerminalNode t: k.SYMBOLIC()) {
				LOGGER.finest(this.myName + " addJcllib kywdCtxList SYMBOLIC() t.getSymbol().getText(): " + t.getSymbol().getText());
			}
			for (TerminalNode t: k.QUOTED_STRING_FRAGMENT()) {
				LOGGER.finest(this.myName + " addJcllib kywdCtxList QUOTED_STRING_FRAGMENT() t.getSymbol().getText(): " + t.getSymbol().getText());
			}
		}

		this.jcllib.addAll(PPKeywordOrSymbolicWrapper.bunchOfThese(kywdCtxList));
	}

	public void setEndLine(int aLine) {
		this.endLine = aLine;
	}

	public void addInstreamProc(PPProc iProc) {
		this.procs.add(iProc);
	}

	public void addSymbolic(PPSetSymbolValue symbolic) {
		this.symbolics.add(symbolic);
	}

	public void addInclude(PPIncludeStatement include) {
		this.includes.add(include);
	}

	public void addJclStep(PPJclStep step) {
		this.steps.add(step);
		if (step.isExecProc()) {
			for (PPProc p: procs) {
				if (step.getProcExecuted().equals(p.getProcName())) {
					step.setProc(p);
					break;
				}
			}
		}
	}

	public void resolveParmedIncludes(ArrayList<PPSetSymbolValue> symbolics) {
		LOGGER.finest(this.myName + " resolveParmedIncludes " + this + " symbolics = |" + symbolics + "|");
		ArrayList<PPSetSymbolValue> mergedSymbolics = new ArrayList<>(symbolics);
		mergedSymbolics.addAll(this.symbolics);

		for (PPIncludeStatement i: this.includes) {
			i.resolveParms(mergedSymbolics);
		}
		LOGGER.finest(this.myName + " includes (after resolving): " + this.includes);

	}

	public void resolveParms(ArrayList<PPSetSymbolValue> symbolics) {
		LOGGER.finest(this.myName + " resolveParms " + this + " symbolics = |" + symbolics + "|");


		for (PPJclStep step: this.steps) {
			ArrayList<PPSetSymbolValue> mergedSymbolics = new ArrayList<>(symbolics);
			for (PPSetSymbolValue s: this.symbolics) {
				if ((s.getSetType() == SetTypeOfSymbolValue.SET && s.getLine() < step.getLine())
				|| s.getSetType() != SetTypeOfSymbolValue.SET
				) {
					mergedSymbolics.add(s);
				}
			}
			step.resolveParms(mergedSymbolics);
		}
	}

	public ArrayList<PPJclStep> stepsInNeedOfProc() {
		ArrayList<PPJclStep> stepsInNeed = new ArrayList<>();

		for (PPJclStep step: this.steps) {
			if (step.isExecProc()) {
				if (step.needsProc()) {
					stepsInNeed.add(step);
				} else {
					stepsInNeed.addAll(step.getProc().stepsInNeedOfProc());
				} 
			}
		}

		return stepsInNeed;
	}

	public Boolean lineIsInInstreamProc(int aLine) {
		Boolean b = false;

		for (PPProc p: this.procs) {
			b = p.containsLine(aLine) && (p.getFileName().equals(this.fileName));
			if (b) break;
		}

		return b;
	}

	public PPProc instreamProcThisLineIsIn(int aLine) {
		PPProc aProc = null;

		for (PPProc p: this.procs) {
			if (p.containsLine(aLine) && (p.getFileName().equals(this.fileName))) {
				aProc = p;
				break;
			}
		}

		return aProc;
	}

	public Boolean lineIsInThisJob(int aLine) {
		return ((aLine >= this.startLine) && (aLine <= this.endLine));
	}

	public PPIncludeStatement includeStatementAt(int aLine) {
		for (PPIncludeStatement i: this.includes) {
			if (i.getLine() == aLine) return i;
		}

		return null;
	}

	public PPJclStep jclStepAt(int aLine) {
		for (PPJclStep j: steps) {
			if (j.getLine() == aLine) return j;
		}

		return null;
	}

	public String getFileName() {
		return this.fileName;
	}

	public UUID getUUID() {
		return this.uuid;
	}

	public String getJobName() {
		return this.jobName;
	}

	public int getStartLine() {
		return this.startLine;
	}

	public int getEndLine() {
		return this.endLine;
	}

	public ArrayList<PPJclStep> getSteps() {
		return this.steps;
	}

	public ArrayList<PPIncludeStatement> getAllIncludes() {
		return this.includes;
	}

	public ArrayList<PPIncludeStatement> getAllUnresolvedIncludes() {
		PPIncludeStatement[] unresolved_includes = 
				this.getAllIncludes().stream()
				.filter(i -> !i.isResolved())
				.toArray(PPIncludeStatement[]::new);
		return new ArrayList<PPIncludeStatement>(Arrays.asList(unresolved_includes));
	}

	public ArrayList<String> getJcllibStrings() {
		ArrayList<String> libs = new ArrayList<>();

		for (PPKeywordOrSymbolicWrapper k: jcllib) {
			libs.add(k.getValue());
		}

		return libs;
	}
/*
	public File rewriteWithParmsResolved(File tmpRootDir, Boolean saveTemp) throws IOException {

	}
*/
	public File rewriteJobWithIncludesResolved(File tmpJobDir, File tmpProcDir, Boolean saveTemp) throws IOException {
		/*
			At this point the intent is to iteratively process the job until all INCLUDEs are
			resolved.  Potentially, an INCLUDE can contain other INCLUDEs, SETs, and EXECs.
		*/
		LOGGER.fine(this.myName + " rewriteJobWithIncludesResolved job = |" + this + "| tmpJobDir = |" + tmpJobDir + "|");

		File aFile = new File(this.getFileName());
		LineNumberReader src = new LineNumberReader(new FileReader(aFile));
		File tmp = new File(tmpJobDir.toString() + File.separator + "job-" + this.getJobName() + "-" + this.getUUID());
		if (saveTemp) {
		} else {
			tmp.deleteOnExit();
		}
		PrintWriter out = new PrintWriter(tmp);
		LOGGER.finest("tmp = |" + tmp.getName() + "|");
		String inLine = new String();
		while ((inLine = src.readLine()) != null) {
			PPIncludeStatement i = this.includeStatementAt(src.getLineNumber());
			if (i == null) {
				out.println(inLine);
			} else {
				if (writeTheIncludeContent(i, out, tmpProcDir)) {
				} else {
					out.println(inLine);
				}
			}
		}
		src.close();
		out.close();
		return tmp;
	}

	public File rewriteJobAndSeparateInstreamProcs(File tmpJobDir, File tmpProcDir) throws IOException {
		/*
			Rewrite one job from the current file, separating any instream procs into their own
			files to be processed later.

			After this point the intent is to iteratively process the job until all INCLUDEs are
			resolved.  Potentially, an INCLUDE can contain other INCLUDEs, SETs, and EXECs.
		*/
		/*
			the plan...

			for each job, read a record from its file
				if the record number resides in an instream proc, skip it
				if the record number corresponds to a resolved include,
					skip writing the include, instead read the file it
					refers to and add that to the output in place of the include
				if the record number corresponds to a jclstep _not_ in stepsInNeedOfProc,
					open a new LineNumberReader on the jclstep's file
					read the proc, writing records to a new file
					if the record number corresponds to a resolved include,
						skip writing the include, instead read the file it
						refers to and add that to the output in place of the include
				write the record read to output
		*/
		LOGGER.fine(this.myName + " rewriteJobAndSeparateInstreamProcs job = |" + this + "| tmpJobDir = |" + tmpJobDir + "| tmpProcDir = |" + tmpProcDir + "|");

		File aFile = new File(this.getFileName());
		LineNumberReader src = new LineNumberReader(new FileReader(aFile));
		File tmp = new File(tmpJobDir.toString() + File.separator + "job-" + this.getJobName() + "-" + this.getUUID());
		if (this.CLI.saveTemp) {
		} else {
			tmp.deleteOnExit();
		}
		PrintWriter out = new PrintWriter(tmp);
		LOGGER.finest("tmp = |" + tmp.getName() + "|");
		String inLine = new String();
		PPProc aProc = null;
		File procTmp = null;
		PrintWriter procOut = null;
		while ((inLine = src.readLine()) != null) {
			if (this.lineIsInThisJob(src.getLineNumber())) {
			} else {
				continue;
			}
			aProc = this.instreamProcThisLineIsIn(src.getLineNumber());
			if (aProc == null) {
				if (procOut == null) {
				} else {
					procOut.close();
					procTmp = null;
					procOut = null;
				}
				PPIncludeStatement i = this.includeStatementAt(src.getLineNumber());
				if (i == null) {
					out.println(inLine);
				} else {
					if (writeTheIncludeContent(i, out, tmpProcDir)) {
					} else {
						out.println(inLine);
					}
				}
			} else {
				if (procOut == null) {
					procTmp = new File(tmpProcDir.toString() + File.separator + aProc.getProcName());
					if (this.CLI.saveTemp) {
					} else {
						procTmp.deleteOnExit();
					}
					procOut = new PrintWriter(procTmp);
					LOGGER.finest("procTmp = |" + procTmp.getName() + "|");
				}
				procOut.println(inLine);
			}
			if (src.getLineNumber() == this.getEndLine()) break; //end of this job in this file
		}
		src.close();
		out.close();
		return tmp;
	}

	public Boolean writeTheIncludeContent(
							PPIncludeStatement i
							, PrintWriter out
							, File tmpProcDir)
						throws IOException {

		LOGGER.fine("writeTheIncludeContent i =|" + i + "| tmpProcDir = |" + tmpProcDir.getName() + "|");

		if (i.isResolved()) {
		} else {
			return false;
		}

		Boolean foundIt = true;
		String includeFile = i.getResolvedText();

		String includeFileFull = searchProcPathsFor(includeFile, tmpProcDir);

		if (includeFileFull == null) {
			foundIt = false;
			//LOGGER.warning(includeFile + " not found in any path specified");
			//throw new FileNotFoundException(copyFile + " not found in any path specified");
		} else {
			List<String> list = 
				Files.readAllLines(Paths.get(includeFileFull));
			for (String line: list) out.println(line);
		}

		return foundIt;
	}

	public String searchProcPathsFor(String fileName, File tmpProcDir) throws IOException {
		File aFile = new File(tmpProcDir.getName() + File.separator + fileName);
		if (aFile.exists()) {
			LOGGER.finer("searchProcPathsFor() found " + aFile.getCanonicalPath());
			return aFile.getCanonicalPath();
		}

		ArrayList<String> jcllib = this.getJcllibStrings();
		for (String lib: jcllib) {
			if (this.CLI.mappedProcPaths.containsKey(lib)) {
				aFile = new File(this.CLI.mappedProcPaths.get(lib) + File.separator + fileName);
				if (aFile.exists()) {
					LOGGER.finer("searchProcPathsFor() found " + aFile.getCanonicalPath());
					return aFile.getCanonicalPath();
				}
			}
		}

		for (String path: this.CLI.staticProcPaths) {
			aFile = new File(path + File.separator + fileName);
			if (aFile.exists()) {
				LOGGER.finer("searchProcPathsFor() found " + aFile.getCanonicalPath());
				return aFile.getCanonicalPath();
			}
		}

		LOGGER.warning("searchProcPathsFor() did not find " + fileName);
		return null;
	}

	public File newTempDir(File appRootDir, Boolean saveTemp) throws IOException {
		/*
			It's possible the file permissions are superfluous.  The code would be more
			portable without them.  TODO maybe remove the code setting file permissions.
		*/
		Set<PosixFilePermission> perms = PosixFilePermissions.fromString("rwxr-x---");
		FileAttribute<Set<PosixFilePermission>> attr =
			PosixFilePermissions.asFileAttribute(perms);
		File tmpDir = Files.createTempDirectory(appRootDir.toString() + File.separator + this.getJobName() + "-", attr).toFile();

		if (saveTemp) {
		} else {
			tmpDir.deleteOnExit();
		}

		return tmpDir;
	}

	public String toString() {
		return 
			this.getJobName() 
			+ " @ " 
			+ this.jobCardCtx.jobName().NAME_FIELD().getSymbol().getLine() 
			+ " in " 
			+ this.fileName;
	}
}

