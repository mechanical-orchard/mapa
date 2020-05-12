
/*
THIS SOFTWARE IS PROVIDED "AS IS" AND ANY EXPRESSED OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE REGENTS OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
*/

import java.util.*;
import java.time.*;
import java.time.format.*;
import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.*;
import java.util.logging.*;
import org.antlr.v4.runtime.*;
import org.antlr.v4.runtime.tree.*;

/**
This is intended to demonstrate use of the JCL lexing and parsing code
generated by the JCL*.g4 files in the ./src directory.

*/

public class Demo01{

public static final Logger LOGGER = Logger.getLogger(Demo01.class.getName());
public static TheCLI CLI = null;

public static void main(String[] args) throws Exception {

	/*
	Housekeeping.  Set up a logger to log messages to a file.
	*/
	Handler fileHandler  = null;
	DateTimeFormatter df = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");
	String dateTimeStamp = LocalDateTime.now().format(df).toString();

	System.setProperty("java.util.logging.SimpleFormatter.format", "[%1$tc] %4$s: %5$s%n");

	try {
		fileHandler = new FileHandler("./" + Demo01.class.getName() + "-" + dateTimeStamp + ".log");
		fileHandler.setFormatter(new SimpleFormatter());
		LOGGER.addHandler(fileHandler);
		fileHandler.setLevel(Level.ALL);
		LOGGER.setLevel(Level.ALL);
		LOGGER.info("Logger Name: " + LOGGER.getName());
	} catch (Exception e) {
		LOGGER.severe("Exception " + e + " encountered");
		e.printStackTrace();
		System.exit(16);
	}

	/*
	Housekeeping.  Parse command line options.
	*/
	CLI = new TheCLI(args);

	File baseDir = newTempDir(); // keep all temp files contained here
	int fileNb = 0;
	BufferedWriter outtree = null;
	BufferedWriter outcsv = null;

	if (CLI.outtreeFileName == null) {
	} else {
        outtree = new BufferedWriter(new FileWriter(CLI.outtreeFileName));
	}

	if (CLI.outcsvFileName == null) {
	} else {
        outcsv = new BufferedWriter(new FileWriter(CLI.outcsvFileName));
	}

	for (String aFileName: CLI.fileNamesToProcess) {
		LOGGER.info("Processing file " + aFileName);
		Boolean first = true;
		fileNb++;
		ArrayList<PPProc> procsPP = new ArrayList<>();
		ArrayList<PPJob> jobsPP = new ArrayList<>();
		int jobNb = 0;
		UUID uuid = UUID.randomUUID(); //identify a file
		File aFileRewritten = rewriteWithoutCol72to80(aFileName, baseDir);
		lexAndParsePP(jobsPP, procsPP, aFileRewritten.getPath(), fileNb);
		if (jobsPP.size() == 0 && procsPP.size() == 0) {
			LOGGER.info(aFileName + " contains neither jobs nor procs - not JCL?");
		}
		for (PPJob j: jobsPP) {
			jobNb++;
			LOGGER.info("Processing job " + j.getJobName());
			j.resolveParmedIncludes();
			LOGGER.finest(j + " stepsInNeedOfProc = " + j.stepsInNeedOfProc());
			File jobFile = j.rewriteJobAndSeparateInstreamProcs(baseDir);
			/*
				Now must iteratively parse this job until all INCLUDEs 
				are resolved.  Unresolvable INCLUDEs generate a warning.
			*/
			PPJob rJob = j.iterativelyResolveIncludes(jobFile);
			/*
				Symbolic parms may have had values SET inside an INCLUDE,
				so only now the INCLUDEs have been resolved can the symbolics 
				be resolved.
			*/
			//rJob.resolveParms();
			/*
				Now must rewrite job with resolved values for parms substituted.
			*/
			File finalJobFile = rJob.rewriteWithParmsResolved();
			rJob.resolveProcs();
			/*
				Now transition from preprocessing to lexing/parsing resolved JCL.
			*/
			ArrayList<Proc> procs = new ArrayList<>();
			ArrayList<Job> jobs = new ArrayList<>();
			lexAndParse(jobs, procs, finalJobFile.getPath(), fileNb);
			jobs.get(0).setTmpDirs(baseDir, rJob.getJobDir(), rJob.getProcDir());
			jobs.get(0).setOrdNb(rJob.getOrdNb());
			jobs.get(0).lexAndParseProcs();
			if (CLI.outtreeFileName == null) {
			} else {
				StringBuffer sb = new StringBuffer();
				sb.append(System.getProperty("line.separator"));
				jobs.get(0).toTree(sb);
		        outtree.write(sb.toString());
				LOGGER.fine(sb.toString());
			}
			if (CLI.outcsvFileName == null) {
			} else {
				StringBuffer buf = new StringBuffer();
				if (first) {
					buf.append(System.getProperty("line.separator"));
					buf.append("FILE");
					buf.append(",");
					buf.append(aFileName);
					buf.append(",");
					buf.append(dateTimeStamp);
					buf.append(",");
					buf.append(uuid.toString());
				}
				buf.append(System.getProperty("line.separator"));
				jobs.get(0).toCSV(buf, uuid);
				outcsv.write(buf.toString());
				LOGGER.fine(buf.toString());
			}
		}
		for (PPProc p: procsPP) {
			LOGGER.info("Processing proc " + p.getProcName());
			p.setTmpDirs(baseDir);
			File procFile = new File(p.getFileName());
			/*
				Now must iteratively parse this proc until all INCLUDEs 
				are resolved.  Unresolvable INCLUDEs generate a warning.
			*/
			ArrayList<PPSetSymbolValue> emptySetSym = new ArrayList<>();
			PPProc rProc = p.iterativelyResolveIncludes(emptySetSym, procFile);
			/*
				Symbolic parms may have had values SET inside an INCLUDE,
				so only now the INCLUDEs have been resolved can the symbolics 
				be resolved.
			*/
			rProc.resolveParms(emptySetSym);
			/*
				Now must rewrite proc with resolved values for parms substituted.
			*/
			File finalProcFile = rProc.rewriteWithParmsResolved();
			rProc.resolveProcs();
			/*
				Now transition from preprocessing to lexing/parsing resolved JCL.
			*/
			ArrayList<Proc> procs = new ArrayList<>();
			ArrayList<Job> jobs = new ArrayList<>();
			lexAndParse(jobs, procs, finalProcFile.getPath(), fileNb);
			procs.get(0).setTmpDirs(baseDir, rProc.getProcDir());
			procs.get(0).setOrdNb(rProc.getOrdNb());
			procs.get(0).lexAndParseProcs();
			if (CLI.outtreeFileName == null) {
			} else {
				StringBuffer sb = new StringBuffer();
				sb.append(System.getProperty("line.separator"));
				procs.get(0).toTree(sb);
		        outtree.write(sb.toString());
				LOGGER.fine(sb.toString());
			}
			if (CLI.outcsvFileName == null) {
			} else {
				StringBuffer buf = new StringBuffer();
				if (first) {
					buf.append(System.getProperty("line.separator"));
					buf.append("FILE");
					buf.append(",");
					buf.append(aFileName);
					buf.append(",");
					buf.append(uuid.toString());
				}
				buf.append(System.getProperty("line.separator"));
				procs.get(0).toCSV(buf, uuid);
				outcsv.write(buf.toString());
				LOGGER.fine(buf.toString());
			}
			first = false;
		}
	}

	if (CLI.outtreeFileName == null) {
	} else {
        outtree.flush();
        outtree.close();
	}

	if (CLI.outcsvFileName == null) {
	} else {
		outcsv.flush();
		outcsv.close();
	}

	LOGGER.info("Processing complete");
}

	public static void lexAndParsePP(
					ArrayList<PPJob> jobs
					, ArrayList<PPProc> procs
					, String fileName
					, int fileNb
					) throws IOException {
		LOGGER.fine("lexAndParsePP jobs = |" + jobs + "| procs = |" + procs + "| fileName = |" + fileName + "|");

		CharStream cs = CharStreams.fromFileName(fileName);  //load the file
		JCLPPLexer.ckCol72 = false;
		JCLPPLexer jcllexer = new JCLPPLexer(cs);  //instantiate a lexer
		CommonTokenStream jcltokens = new CommonTokenStream(jcllexer); //scan stream for tokens
		JCLPPParser jclparser = new JCLPPParser(jcltokens);  //parse the tokens	

		ParseTree jcltree = jclparser.startRule(); // parse the content and get the tree
	
		ParseTreeWalker jclwalker = new ParseTreeWalker();
	
		PPListener jobListener = new PPListener(jobs, procs, fileName, fileNb, LOGGER, CLI);
	
		LOGGER.finer("----------walking tree with " + jobListener.getClass().getName());
	
		jclwalker.walk(jobListener, jcltree);

	}

	public static ArrayList<Token> lex(
					String fileName
					) throws IOException {
		LOGGER.fine("lex fileName = |" + fileName + "|");

		CharStream cs = CharStreams.fromFileName(fileName);  //load the file
		JCLPPLexer.ckCol72 = true;
		JCLPPLexer jcllexer = new JCLPPLexer(cs);  //instantiate a lexer
		CommonTokenStream cmtokens = new CommonTokenStream(jcllexer, JCLPPLexer.COMMENTS); //scan stream for tokens
		ArrayList<Token> tokens = new ArrayList<>();
		while (cmtokens.LA(1) != CommonTokenStream.EOF) {
			if (cmtokens.LT(1).getType() == JCLPPLexer.COL_72 || cmtokens.LT(1).getType() == JCLPPLexer.COMMENT_TEXT) {
				tokens.add(cmtokens.LT(1));
			}
			cmtokens.consume();
		}
		for (Token t: tokens) {
			LOGGER.fine(
				"\ttoken |" 
				+ t.getText()
				+ "| @ "
				+ t.getCharPositionInLine()
				+ " on "
				+ t.getLine()
				+ " of type "
				+ t.getType()
				);
		}

		return tokens;
	}

	public static void lexAndParse(
					ArrayList<Job> jobs
					, ArrayList<Proc> procs
					, String fileName
					, int fileNb
					) throws IOException {
		LOGGER.fine("lexAndParse jobs = |" + jobs + "| procs = |" + procs + "| fileName = |" + fileName + "|");

		CharStream cs = CharStreams.fromFileName(fileName);  //load the file
		JCLLexer jcllexer = new JCLLexer(cs);  //instantiate a lexer
		CommonTokenStream jcltokens = new CommonTokenStream(jcllexer); //scan stream for tokens
		JCLParser jclparser = new JCLParser(jcltokens);  //parse the tokens	

		ParseTree jcltree = jclparser.startRule(); // parse the content and get the tree
	
		ParseTreeWalker jclwalker = new ParseTreeWalker();
	
		JobListener jobListener = new JobListener(jobs, procs, fileName, fileNb, LOGGER, CLI);
	
		LOGGER.finer("----------walking tree with " + jobListener.getClass().getName());
	
		jclwalker.walk(jobListener, jcltree);

	}

	/**
	This method rewrites a file without the troublesome columns 72 
	through 80.
	*/
	private static File rewriteWithoutCol72to80(String aFileName, File baseDir) throws IOException {
		LOGGER.finer(
			"Demo01" 
			+ " rewriteWithoutCol72to80"
			);

		ArrayList<Token> tokens = lex(aFileName);
		File aFile = new File(aFileName);
		LineNumberReader src = new LineNumberReader(new FileReader(aFile));
		File tmp = new File(
			baseDir.toString() 
			+ File.separator 
			+ aFile.getName()
			+ "-" 
			+ UUID.randomUUID()
			);
		if (CLI.saveTemp) {
		} else {
			tmp.deleteOnExit();
		}
		PrintWriter out = new PrintWriter(tmp);
		LOGGER.finest("Demo01" + " tmp = |" + tmp.getName() + "|");
		String inLine = new String();
		Boolean addSplat = false;
		while ((inLine = src.readLine()) != null) {
			StringBuilder newLine = new StringBuilder(inLine);
			ArrayList<Token> onThisLine = new ArrayList<>();
			Token col72 = null;
			Token cmBefore72 = null;
			Token cmAfter72 = null;
			for (Token t: tokens) {
				if (t.getLine() == src.getLineNumber()) {
					onThisLine.add(t);
					if (t.getType() == JCLPPLexer.COMMENT_TEXT) {
						if (t.getText().trim().length() > 0) {
							if (t.getCharPositionInLine() < 71) {
								cmBefore72 = t;
							} else {
								cmAfter72 = t;
							}
						}
					}
					if (t.getType() == JCLPPLexer.COL_72) {
						col72 = t;
					}
				}
			}
			if (addSplat) {
				/*
				Note that the splat is added to the line _after_ the column 72
				comment continuation was found.
				*/
				newLine.setCharAt(2, '*');
			}
			if (onThisLine.size() > 0) {
				if (cmBefore72 != null && col72 != null) {
					/*
					Next line is a comment because this line has a comment
					_and_ a continuation indicator in column 72.
					*/
					addSplat = true;
				} else {
					addSplat = false;
				}
				if (cmAfter72 != null) {
					int start = cmAfter72.getCharPositionInLine();
					int end = start + cmAfter72.getText().length();
					String spaces = String.format("%1$"+ ((end - start) + 1) + "s", " ");
					newLine.replace(start, end, spaces);
				}
				if (col72 != null) {
					newLine.setCharAt(71, ' ');
				}
			}
			out.println(newLine.toString());
		}
		src.close();
		out.close();
		if (tmp.toPath().getFileSystem().supportedFileAttributeViews().contains("posix")) {
			Set<PosixFilePermission> perms = PosixFilePermissions.fromString("rw-r-----");
			Files.setPosixFilePermissions(tmp.toPath(), perms);
		}
		return tmp;
	}

	public static File newTempDir() throws IOException {
		File tmpDir = Files.createTempDirectory("Demo01-").toFile();
		if (tmpDir.toPath().getFileSystem().supportedFileAttributeViews().contains("posix")) {
			Set<PosixFilePermission> perms = PosixFilePermissions.fromString("rwxr-x---");
			Files.setPosixFilePermissions(tmpDir.toPath(), perms);
		}

		if (CLI.saveTemp) {
		} else {
			tmpDir.deleteOnExit();
		}

		return tmpDir;
	}

}
