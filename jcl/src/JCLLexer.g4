/*
Copyright (C) 2019, Craig Schneiderwent.  All rights reserved.

This software may be modified and distributed under the terms
of the MIT license. See the LICENSE file for details.

JCL may refer to Execution JCL or Cataloged Procedures or fragments
stored in a library referred to by a JCLLIB statement and brought
into the jobstream at execution time via INCLUDE statements. 

A job consists of a JOB statement followed by one or more STEPs.

A STEP consists of an EXEC statement followed by one or more DD
statements and OUTPUT statements.

An EXEC statement may execute a PGM or a PROC.

Why all the modes?

//[NAME] OP [PARAMETER[,PARAMETER[...]]] COMMENTS COMMENTS COMMENTS
00000000011111111112222222222333333333344444444445555555555666666666677777777778
12345678901234567890123456789012345678901234567890123456789012345678901234567890

So, NAME is optional but the variable # spaces following are not, followed
by OP (operation) followed by some white space, followed by parameters,
followed by white space, optionally followed by comments.

Or...

//            [mandatory]
NAME          [optional]
whitespace    [mandatory]
OPERATION     [mandatory]
whitespace    [mandatory]
PARAMETERs    [optional, depends on OPERATION]
whitespace    [indicates what follows are comments]
COMMENTS

...so whitespace is a delimiter and sometimes indicates what follows
is comments and not to be recognized as parameters.

//AEIOU JOB
//ELBOW JOB 'HI THERE',TIME=7 HERE IS A COMMENT
//ZEBRA EXEC PGM=J8765309 ,PARM='THIS IS A COMMENT STARTING AT THE COMMA'
//DD001 DD  DISP=SHR,DSN=SYS1.PARMLIB

And that's why all the modes.  So white space can ->mode(CM).

Also, it is difficult to overstate the ugliness of the DLM parameter in
conjunction with DD * and DD DATA.  More modes, along with globals.

{System.out.println(getLine() + ":" + getCharPositionInLine() + " / " + getText() + "/");}
*/

lexer grammar JCLLexer;

@lexer::members {

    public java.util.Set<String> defaultDlmVals = new java.util.HashSet<String>(){{
      add("//");
      add("/*");
    }};
    public java.util.ArrayList<String> dlmVals = new java.util.ArrayList(defaultDlmVals);

}

tokens { COMMENT_FLAG , CNTL , COMMAND , DD , ELSE , ENDCNTL , ENDIF , EXEC , IF , INCLUDE , JCLLIB , JOB , OUTPUT , PEND , PROC , SCHEDULE , SET , XMIT, EQUAL , ACCODE , AMP , ASTERISK , AVGREC , BLKSIZE ,  BLKSZLIM , BURST , CCSID , CHARS , CHKPT , COPIES , DATA , DATACLAS , DCB , DDNAME , DEST , DISP , DLM , DSID , DSKEYLBL , DSNAME , DSNTYPE , DUMMY , DYNAM , EATTR , EXPDT , FCB , FILEDATA , FLASH , FREE , FREEVOL , GDGORDER , HOLD , KEYLABL1 , KEYLABL2 , KEYENCD1 , KEYENCD2 , KEYLEN , KEYOFF , LABEL , LGSTREAM , LIKE , LRECL , MAXGENS , MGMTCLAS , MODIFY , OUTLIM , OUTPUT , PATH , PATHDISP , PATHMODE , PATHOPTS , PROTECT , RECFM , RECORG , REFDD , RETPD , RLS , ROACCESS , SECMODEL , SEGMENT , SPACE , SPIN , STORCLAS , SUBSYS , SYMBOLS , SYMLIST , SYSOUT , TERM , UCS , UNIT , VOLUME }

// lexer rules --------------------------------------------------------------------------------

SS : SLASH SLASH {getCharPositionInLine() == 2}? ->mode(NM) ;
LINE_NB : ANYCHAR ANYCHAR ANYCHAR ANYCHAR ANYCHAR ANYCHAR ANYCHAR ANYCHAR {getCharPositionInLine() == 80}? -> skip;
COMMENT_FLAG : SLASH SLASH ASTERISK {getCharPositionInLine() == 3}? ->mode(CM);
COMMENT_FLAG_INLINE : COMMA ' ' ->mode(CM) ;
//NAME_FIELD : NAME (DOT NAME)? {System.out.println("NAME_FIELD found " + getVocabulary().getSymbolicName(myTerminalNode.getSymbol().getType()));} ->mode(OP) ;
COND_OP : COND_EQ | COND_GE | COND_GT | COND_LE | COND_LT | COND_NE ;
OUTPUT_LIMIT_OPTION : CANCEL | DUMP | WARNING ;
MEM_UNIT : K | M | G | T | P ;
SYMBOLIC : AMPERSAND [A-Z0-9@#$]+ ;

ACCODE : A C C O D E ;
ACCBIAS : A C C B I A S ;
ACCT : A C C T ;
ADDRSPC : A D D R S P C ;
ALL : A L L ;
ALLOW : A L L O W ;
fragment ALPHA : [A-Z] ;
AMORG : A M O R G ;
AMP : A M P ;
AMPERSAND : '&' ;
ANY : A N Y ;
ASTERISK : '*' ;
AVGREC : A V G R E C ;
AVGREC_UNIT : U | K | M ;
BACKOUT : B A C K O U T ;
BLKSIZE : B L K S I Z E ;
BLKSZLIM : B L K S Z L I M ;
BUFND : B U F N D ;
BUFNI : B U F N I ;
BUFNO : B U F N O ;
BUFF : B U F F ;
BUFSP : B U F S P ;
BURST : B U R S T ;
BYTES : B Y T E S ; 
CANCEL : C A N C E L ;
CARDS : C A R D S ;
CB : C B ;
CCSID : C C S I D ;
CHARS : C H A R S ;
CHKPT : C H K P T ;
CLASS : C L A S S ->mode(CL) ;
CNTL_DFLT : C N T L ->mode(POST_OP),type(CNTL) ;
COMMA : ',' ;
COMMAND_DFLT : C O M M A N D ->mode(POST_OP),type(COMMAND) ;
COMMIT : C O M M I T ;
COND : C O N D ;
COND_EQ : E Q ;
COND_GE : G E ;
COND_GT : G T ;
COND_LE : L E ;
COND_LT : L T ;
COND_NE : N E ;
COPIES : C O P I E S ;
COPY : C O P Y ;
CROPS : C R O P S ;
DATA : D A T A ;
DATACLAS : D A T A C L A S ;
DCB : D C B ;
DD_DFLT : D D ->mode(DD_OP),type(DD) ;
DDNAME : D D N A M E ;
DEST : D E S T ;
DIAGNS : D I A G N S ;
DISALLOW : D I S A L L O W ;
DISP : D I S P ;
DLM : D L M ->pushMode(DLM_MODE) ;
DO : D O ;
DOT : '.' ;
DQUOTE : '"' ;
DSENQSHR : D S E N Q S H R ;
DSID : D S I D ;
DSKEYLBL : D S K E Y L B L ;
DSN : D S N ;
DSNAME : D S N A M E ;
DSNTYPE : D S N T Y P E ;
DUMMY : D U M M Y ;
DUMP : D U M P ;
DW : D W ;
DYNAM : D Y N A M ;
DYNAMNBR : D Y N A M N B R ;
EATTR : E A T T R ;
ECODE : E C O D E ;
ELSE_DFLT : E L S E ->mode(POST_OP),type(ELSE) ;
EMAIL : E M A I L ;
ENDCNTL_DFLT : E N D C N T L ->mode(POST_OP),type(ENDCNTL) ;
ENDIF_DFLT : E N D I F ->mode(POST_OP),type(ENDIF) ;
EOV : E O V ;
EQUAL : '=' ;
EVEN : E V E N ;
EXEC_DFLT : E X E C ->mode(EX),type(EXEC) ;
EXPDT : E X P D T ;
FCB : F C B ;
FILEDATA : F I L E D A T A ;
FLASH : F L A S H ;
FOURTEENFORTY : '1' '4' '4' '0' ;
FREE : F R E E ;
FREEVOL : F R E E V O L ;
FRLOG : F R L O G ;
GDGBIAS : G D G B I A S ;
GDGORDER : G D G O R D E R ;
GROUP : G R O U P ;
HOLD : H O L D ;
HOOK : H O O K ;
HYPHEN : '-' ;
IF_DFLT : I F ->mode(POST_OP),type(IF) ;
INCLUDE_DFLT : I N C L U D E ->mode(POST_OP),type(INCLUDE) ;
IND : I N D ;
JCLHOLD : J C L H O L D ;
JCLLIB_DFLT : J C L L I B ->mode(POST_OP),type(JCLLIB) ;
JESLOG : J E S L O G ;
JGLOBAL : J G L O B A L ;
JLOCAL : J L O C A L ;
JOB_DFLT : J O B ->mode(POST_OP),type(JOB) ;
JOBLIB : J O B L I B ;
JOBRC : J O B R C ;
KEY : K E Y ;
KEYLABL1 : K E Y L A B L '1' ;
KEYLABL2 : K E Y L A B L '2' ;
KEYENCD1 : K E Y E N C D '1' ;
KEYENCD2 : K E Y E N C D '2' ;
KEYLEN : K E Y L E N ;
KEYOFF : K E Y O F F ;
LABEL : L A B E L ;
LASTRC : L A S T R C ;
LGSTREAM : L G S T R E A M ;
LIKE : L I K E ;
LINES : L I N E S ;
LPAREN : '(' ;
LRECL : L R E C L ;
MAXGENS : M A X G E N S ;
MAXIMUM : M A X I M U M ;
MAXRC : M A X R C ;
MEMLIMIT : M E M L I M I T ;
MGMTCLAS : M G M T C L A S ;
MODE : M O D E ;
MODIFY : M O D I F Y ;
MSG : M S G ;
MSGCLASS : M S G C L A S S ;
MSGLEVEL : M S G L E V E L ;
fragment NATL : [@#$] ;
NC : N C ;
NCK : N C K ;
NEWLINE : [\n\r] ->channel(HIDDEN),mode(DEFAULT_MODE) ;
NOLIMIT : N O L I M I T ;
NONE : N O N E ;
NOSPIN : N O S P I N ;
NOTIFY : N O T I F Y ;
NR : N R ;
NRC : N R C ;
NRE : N R E ;
fragment NUM : [0-9] ;
ONLY : O N L Y ;
OPTCD : O P T C D ;
OPTCD_I : I ;
OPTCD_L : L ;
OPTCD_IL : I L ;
OUTLIM : O U T L I M ;
OUTPUT : O U T P U T ;
OUTPUT_DFLT : O U T P U T ->mode(POST_OP),type(OUTPUT) ;
PAGES : P A G E S ;
PARM : P A R M ;
PARMDD : P A R M D D ;
PARM1 : P A R M '1' ;
PARM2 : P A R M '2' ;
PASSWORD : P A S S W O R D ;
PATH : P A T H ;
PATHDISP : P A T H D I S P ;
PATHMODE : P A T H M O D E ;
PATHOPTS : P A T H O P T S ;
PEND_DFLT : P E N D ->mode(POST_OP),type(PEND) ;
PERFORM : P E R F O R M ;
PROC_DFLT : P R O C ->mode(POST_OP),type(PROC) ;
PROTECT : P R O T E C T ;
PRTY : P R T Y ;
RCK : R C K ;
RD : R D ->mode(RD_MODE) ;
REAL : R E A L ;
RECFM : R E C F M ;
RECFM_F : F ;
RECFM_FB : F B ;
RECFM_V : V ;
RECFM_VB : V B ;
RECORG : R E C O R G ;
REDO : R E D O ;
REFDD : R E F D D ;
REGION : R E G I O N ;
REGIONX : R E G I O N X ;
RESTART : R E S T A R T ;
RETPD : R E T P D ;
RLS : R L S ;
RLSTMOUT : R L S T M O U T ;
RMODE31 : R M O D E '3' '1' ;
RNC : R N C ;
ROACCESS : R O A C C E S S ;
RPAREN : ')' ;
SCAN : S C A N ;
SCHEDULE_DFLT : S C H E D U L E ->mode(POST_OP),type(SCHEDULE) ;
SCHENV : S C H E N V ;
SECLABEL : S E C L A B E L ;
SECMODEL : S E C M O D E L ;
SEGMENT : S E G M E N T ;
SER : S E R ;
SET_DFLT : S E T ->mode(POST_OP),type(SET) ;
SLASH : '/' ;
SMBBIAS : S M B B I A S ;
SMBDFR : S M B D F R ;
N_ : N ;
Y_ : Y ;
NO_ : N O ;
YES_ : Y E S ;
SMBHWT : S M B H W T ;
SMBVSP : S M B V S P ;
SMBVSPI : S M B V S P I ;
SO : S O ;
SPACE : S P A C E ;
SPIN : S P I N ;
SQUOTE : '\'' ;
SQUOTE2 : SQUOTE SQUOTE ;
STEP : S T E P ;
STORCLAS : S T O R C L A S ;
STRNO : S T R N O ;
SUBSYS : S U B S Y S ;
SUPPRESS : S U P P R E S S ;
SW : S W ;
SYMBOLS : S Y M B O L S ;
SYMLIST : S Y M L I S T ;
SYNAD : S Y N A D ;
SYSAFF : S Y S A F F ;
SYSOUT : S Y S O U T ;
SYSTEM : S Y S T E M ;
TERM : T E R M ;
TIME : T I M E ;
TRACE : T R A C E ;
TVSMSG : T V S M S G ;
TVSAMCOM : T V S A M C O M ;
TYPRUN : T Y P R U N ;
UCS : U C S ;
UJOBCORR : U J O B C O R R ;
UNIT : U N I T ;
USCORE : '_' ;
USEJC : U S E J C ;
USER : U S E R ;
VIRT : V I R T ;
VOLUME : V O L U M E ;
WARNING : W A R N I N G ;
WS : [ ]+ ->channel(HIDDEN),mode(CM) ;
XMIT_DFLT : X M I T ->mode(POST_OP),type(XMIT) ;

fragment ANYCHAR : ~[\n\r] ;
NAME : (NATL | ALPHA) (ALPHA | NATL | NUM)+ {getText().length() < 9}? ;
NUM_LIT : NUM+ ;
ALNUMNAT4 : (ALPHA | NATL | NUM)+ {getText().length() < 5}? ;
ALNUMNAT8 : (ALPHA | NATL | NUM)+ {getText().length() < 9}? ;
ALNUMNAT16 : (ALPHA | NATL | NUM)+ {getText().length() < 17}? ;
QUOTED_STRING : SQUOTE (~['\n\r] | SQUOTE2)+ SQUOTE ;
SIMPLE_STRING : (ALPHA | NATL | NUM | HYPHEN | USCORE)+ ;
UNQUOTED_STRING : (~['\n\r] | SQUOTE2)+? ;


fragment A:'A';
fragment B:'B';
fragment C:'C';
fragment D:'D';
fragment E:'E';
fragment F:'F';
fragment G:'G';
fragment H:'H';
fragment I:'I';
fragment J:'J';
fragment K:'K';
fragment L:'L';
fragment M:'M';
fragment N:'N';
fragment O:'O';
fragment P:'P';
fragment Q:'Q';
fragment R:'R';
fragment S:'S';
fragment T:'T';
fragment U:'U';
fragment V:'V';
fragment W:'W';
fragment X:'X';
fragment Y:'Y';
fragment Z:'Z';



mode CM ;

COMMENT_TEXT : (' ' | ANYCHAR)+ NEWLINE ->mode(DEFAULT_MODE) ;

mode NM ;

NAME_FIELD : NAME (DOT NAME)? ->mode(OP) ;
CONTINUATION_WS : ' '+ {getText().length() <= 13}? ->channel(HIDDEN),mode(DEFAULT_MODE) ;

mode OP ;

//OPERATION : (CNTL | COMMAND | DD | ELSE | ENDCNTL | ENDIF | EXEC | IF | INCLUDE | JCLLIB | JOB | OUTPUT | PEND | PROC | SCHEDULE | SET | XMIT) ->mode(POST_OP) ;

CNTL_OP : C N T L ->mode(POST_OP),type(CNTL) ;
COMMAND_OP : C O M M A N D ->mode(POST_OP),type(COMMAND) ;
DD_OP : D D->mode(DD_OP),type(DD) ;
ELSE_OP : E L S E ->mode(POST_OP),type(ELSE) ;
ENDCNTL_OP : E N D C N T L ->mode(POST_OP),type(ENDCNTL) ;
ENDIF_OP : E N D I F ->mode(POST_OP),type(ENDIF) ;
EXEC_OP : E X E C ->mode(EX),type(EXEC) ;
IF_OP : I F ->mode(POST_OP),type(IF) ;
INCLUDE_OP : I N C L U D E ->mode(POST_OP),type(INCLUDE) ;
JCLLIB_OP : J C L L I B ->mode(POST_OP),type(JCLLIB) ;
JOB_OP : J O B ->mode(POST_OP),type(JOB) ;
OUTPUT_OP : O U T P U T ->mode(POST_OP),type(OUTPUT) ;
PEND_OP : P E N D ->mode(POST_OP),type(PEND) ;
PROC_OP : P R O C ->mode(POST_OP),type(PROC) ;
SCHEDULE_OP : S C H E D U L E ->mode(POST_OP),type(SCHEDULE) ;
SET_OP : S E T ->mode(POST_OP),type(SET) ;
XMIT_OP : X M I T ->mode(POST_OP),type(XMIT) ;

WS_OP : [ ]+ ->channel(HIDDEN) ;

mode POST_OP ;

WS_POST_OP : [ ]+ ->channel(HIDDEN),mode(DEFAULT_MODE) ;
NEWLINE_POST_OP : [\n\r] ->channel(HIDDEN),mode(DEFAULT_MODE) ;

mode RD_MODE ;

RD_VALUE : (R | RNC | NR | NC) ->mode(DEFAULT_MODE);

mode EX ;

/*
This is a specialized version of POST_OP, it exists because PROC is
both an operation and a parameter...

//DOTHIS PROC 

...and...

//DOTHIS EXEC PROC=P99

...not to mention...

//DOTHIS EXEC P99

...and we must behave differently.  
*/

WS_POST_EX : [ ]+ ->channel(HIDDEN) ;
NEWLINE_POST_EX : [\n\r] ->channel(HIDDEN),mode(DEFAULT_MODE) ;
PGM : P G M ;
PROC_EX : P R O C ;
EQUAL_EX : EQUAL ->type(EQUAL) ;
NAME_EX : NAME ->mode(DEFAULT_MODE) ;

mode CL ;

EQUAL_CL : EQUAL ->type(EQUAL) ;
CLASS_VAL : (NUM | ALPHA) (ALPHA | NATL | NUM)* {getText().length() < 9}? ->mode(DEFAULT_MODE) ;


mode DD_OP ;

WS_DD_OP : [ ]+ ->channel(HIDDEN) ;
NEWLINE_DD_OP : [\n\r] ->channel(HIDDEN),mode(DEFAULT_MODE) ;

DD_ACCODE : ACCODE ->type(ACCODE),mode(DEFAULT_MODE) ;
DD_AMP : AMP ->type(AMP),mode(DEFAULT_MODE) ;
DD_AVGREC : AVGREC ->type(AVGREC),mode(DEFAULT_MODE) ;
DD_ASTERISK : '*' ->type(ASTERISK),mode(DATA_PARM_MODE) ;
DD_BLKSIZE : BLKSIZE ->type(BLKSIZE),mode(DEFAULT_MODE) ;
DD_BLKSZLIM : BLKSZLIM ->type(BLKSZLIM),mode(DEFAULT_MODE) ;
DD_BURST : BURST ->type(BURST),mode(DEFAULT_MODE) ;
DD_CCSID : CCSID ->type(CCSID),mode(DEFAULT_MODE) ;
DD_CHARS : CHARS ->type(CHARS),mode(DEFAULT_MODE) ;
DD_CHKPT : CHKPT ->type(CHKPT),mode(DEFAULT_MODE) ;
DD_CNTL : CNTL_DFLT ->type(CNTL),mode(DEFAULT_MODE) ;
DD_COPIES : COPIES ->type(COPIES),mode(DEFAULT_MODE) ;
DD_DATA : DATA ->type(DATA),mode(DATA_PARM_MODE) ;
DD_DATACLAS : DATACLAS ->type(DATACLAS),mode(DEFAULT_MODE) ;
DD_DCB : DCB ->type(DCB),mode(DEFAULT_MODE) ;
DD_DDNAME : DDNAME ->type(DDNAME),mode(DEFAULT_MODE) ;
DD_DEST : DEST ->type(DEST),mode(DEFAULT_MODE) ;
DD_DISP : DISP ->type(DISP),mode(DEFAULT_MODE) ;
DD_DLM : DLM ->type(DLM),mode(DEFAULT_MODE) ;
DD_DSID : DSID ->type(DSID),mode(DEFAULT_MODE) ;
DD_DSKEYLBL : DSKEYLBL ->type(DSKEYLBL),mode(DEFAULT_MODE) ;
DD_DSN : DSN ->type(DSN),mode(DEFAULT_MODE) ;
DD_DSNAME : DSNAME ->type(DSNAME),mode(DEFAULT_MODE) ;
DD_DSNTYPE : DSNTYPE ->type(DSNTYPE),mode(DEFAULT_MODE) ;
DD_DUMMY : DUMMY ->type(DUMMY),mode(DEFAULT_MODE) ;
DD_DYNAM : DYNAM ->type(DYNAM),mode(DEFAULT_MODE) ;
DD_EATTR : EATTR ->type(EATTR),mode(DEFAULT_MODE) ;
DD_EXPDT : EXPDT ->type(EXPDT),mode(DEFAULT_MODE) ;
DD_FCB : FCB ->type(FCB),mode(DEFAULT_MODE) ;
DD_FILEDATA : FILEDATA ->type(FILEDATA),mode(DEFAULT_MODE) ;
DD_FLASH : FLASH ->type(FLASH),mode(DEFAULT_MODE) ;
DD_FREE : FREE ->type(FREE),mode(DEFAULT_MODE) ;
DD_FREEVOL : FREEVOL ->type(FREEVOL),mode(DEFAULT_MODE) ;
DD_GDGORDER : GDGORDER ->type(GDGORDER),mode(DEFAULT_MODE) ;
DD_HOLD : HOLD ->type(HOLD),mode(DEFAULT_MODE) ;
DD_KEYLABL1 : KEYLABL1 ->type(KEYLABL1),mode(DEFAULT_MODE) ;
DD_KEYLABL2 : KEYLABL2 ->type(KEYLABL2),mode(DEFAULT_MODE) ;
DD_KEYENCD1 : KEYENCD1 ->type(KEYENCD1),mode(DEFAULT_MODE) ;
DD_KEYENCD2 : KEYENCD2 ->type(KEYENCD2),mode(DEFAULT_MODE) ;
DD_KEYLEN : KEYLEN ->type(KEYLEN),mode(DEFAULT_MODE) ;
DD_KEYOFF : KEYOFF ->type(KEYOFF),mode(DEFAULT_MODE) ;
DD_LABEL : LABEL ->type(LABEL),mode(DEFAULT_MODE) ;
DD_LGSTREAM : LGSTREAM ->type(LGSTREAM),mode(DEFAULT_MODE) ;
DD_LIKE : LIKE ->type(LIKE),mode(DEFAULT_MODE) ;
DD_LRECL : LRECL ->type(LRECL),mode(DEFAULT_MODE) ;
DD_MAXGENS : MAXGENS ->type(MAXGENS),mode(DEFAULT_MODE) ;
DD_MGMTCLAS : MGMTCLAS ->type(MGMTCLAS),mode(DEFAULT_MODE) ;
DD_MODIFY : MODIFY ->type(MODIFY),mode(DEFAULT_MODE) ;
DD_OUTLIM : OUTLIM ->type(OUTLIM),mode(DEFAULT_MODE) ;
DD_OUTPUT : OUTPUT ->type(OUTPUT),mode(DEFAULT_MODE) ;
DD_PATH : PATH ->type(PATH),mode(DEFAULT_MODE) ;
DD_PATHDISP : PATHDISP ->type(PATHDISP),mode(DEFAULT_MODE) ;
DD_PATHMODE : PATHMODE ->type(PATHMODE),mode(DEFAULT_MODE) ;
DD_PATHOPTS : PATHOPTS ->type(PATHOPTS),mode(DEFAULT_MODE) ;
DD_PROTECT : PROTECT ->type(PROTECT),mode(DEFAULT_MODE) ;
DD_RECFM : RECFM ->type(RECFM),mode(DEFAULT_MODE) ;
DD_RECORG : RECORG ->type(RECORG),mode(DEFAULT_MODE) ;
DD_REFDD : REFDD ->type(REFDD),mode(DEFAULT_MODE) ;
DD_RETPD : RETPD ->type(RETPD),mode(DEFAULT_MODE) ;
DD_RLS : RLS ->type(RLS),mode(DEFAULT_MODE) ;
DD_ROACCESS : ROACCESS ->type(ROACCESS),mode(DEFAULT_MODE) ;
DD_SECMODEL : SECMODEL ->type(SECMODEL),mode(DEFAULT_MODE) ;
DD_SEGMENT : SEGMENT ->type(SEGMENT),mode(DEFAULT_MODE) ;
DD_SPACE : SPACE ->type(SPACE),mode(DEFAULT_MODE) ;
DD_SPIN : SPIN ->type(SPIN),mode(DEFAULT_MODE) ;
DD_STORCLAS : STORCLAS ->type(STORCLAS),mode(DEFAULT_MODE) ;
DD_SUBSYS : SUBSYS ->type(SUBSYS),mode(DEFAULT_MODE) ;
DD_SYMBOLS : SYMBOLS ->type(SYMBOLS),mode(DEFAULT_MODE) ;
DD_SYMLIST : SYMLIST ->type(SYMLIST),mode(DEFAULT_MODE) ;
DD_SYSOUT : SYSOUT ->type(SYSOUT),mode(DEFAULT_MODE) ;
DD_TERM : TERM ->type(TERM),mode(DEFAULT_MODE) ;
DD_UCS : UCS ->type(UCS),mode(DEFAULT_MODE) ;
DD_UNIT : UNIT ->type(UNIT),mode(DEFAULT_MODE) ;
DD_VOLUME : VOLUME ->type(VOLUME),mode(DEFAULT_MODE) ;

mode DATA_PARM_MODE ;

NEWLINE_DATA_PARM_MODE : [\n\r] ->channel(HIDDEN),mode(DATA_MODE) ;

DATA_PARM_MODE_BLKSIZE : DCB EQUAL BLKSIZE ;
DATA_PARM_MODE_BUFNO : DCB EQUAL BUFNO ;
DATA_PARM_MODE_DIAGNS : DCB EQUAL DIAGNS ;
DATA_PARM_MODE_LRECL : DCB EQUAL LRECL ;
DATA_PARM_MODE_DLM : DLM ->type(DLM),pushMode(DLM_MODE) ;
DATA_PARM_MODE_DSID : DSID ->type(DSID) ;
DATA_PARM_MODE_LIKE : LIKE ->type(LIKE) ;
DATA_PARM_MODE_LRECL1 : LRECL ->type(LRECL) ;
DATA_PARM_MODE_REFDD : REFDD ->type(REFDD) ;
DATA_PARM_MODE_MODEC : DCB EQUAL MODE EQUAL C ;
DATA_PARM_MODE_DSNAME : DSNAME ->type(DSNAME) ;
DATA_PARM_MODE_VOLSER : VOLUME EQUAL SER ;

mode DLM_MODE ;

DLM_EQUAL : EQUAL ->type(EQUAL);
DLM_WS : [ ]+ ->channel(HIDDEN) ;
DLM_VAL : (SIMPLE_STRING | QUOTED_STRING) {dlmVals = new java.util.ArrayList(); dlmVals.add(getText());} ->popMode ;

mode DATA_MODE ;

NEWLINE_DATA_MODE : [\n\r] ->channel(HIDDEN) ;

DATA_MODE_TERMINATOR1 : SLASH SLASH ASTERISK {dlmVals.contains("//") && getCharPositionInLine() == 3}? ->type(COMMENT_FLAG),mode(CM);
DATA_MODE_TERMINATOR2 : SLASH SLASH {dlmVals.contains("//") && getCharPositionInLine() == 2}? ->type(SS),mode(NM) ;
DATA_MODE_TERMINATOR3 : SLASH ASTERISK {dlmVals.contains("/*") && getCharPositionInLine() == 2}? ->mode(DEFAULT_MODE) ;
DATA_MODE_TERMINATORX : ANYCHAR ANYCHAR {dlmVals.contains(getText())}? {dlmVals = new java.util.ArrayList(defaultDlmVals);} ->mode(DEFAULT_MODE) ;
DD_ASTERISK_DATA : (' ' | ANYCHAR)+? ;

//END_JOB_STMT : (ADDRSPC_PARM | BYTES_PARM | CARDS_PARM | CCSID_PARM | CLASS_PARM | COND_PARM | EMAIL_PARM | GROUP_PARM | JESLOG_PARM | JOBRC_PARM | LINES_PARM | MEMLIMIT_PARM | MSGCLASS_PARM | MSGLEVEL_PARM | NOTIFY_PARM | PAGES_PARM | PASSWORD_PARM | PERFORM_PARM | PRTY_PARM | RD_PARM | REGION_PARM | RESTART_PARM | SECLABEL_PARM | SCHENV_PARM | SYSAFF_PARM | SYSTEM_PARM | TIME_PARM | TYPRUN_PARM | USER_PARM) WS_JOB ->popMode,popMode,popMode,mode(CM) ;

/*
ADDRSPC_PARM : ADDRSPC EQUAL (REAL | VIRT) ;
BYTES_PARM : BYTES EQUAL NUM_LIT (COMMA (CANCEL | DUMP | WARNING))? ;
CARDS_PARM : CARDS EQUAL NUM_LIT (COMMA (CANCEL | DUMP | WARNING))? ;
CCSID_PARM : CCSID EQUAL NUM_LIT ;
CLASS_PARM : CLASS EQUAL SIMPLE_STRING ;
COND_PARM : COND EQUAL LPAREN? LPAREN NUM_LIT COMMA COND_OP RPAREN (COMMA LPAREN NUM_LIT COMMA COND_OP RPAREN)* RPAREN? ;
EMAIL_PARM : EMAIL EQUAL SIMPLE_STRING ;
GROUP_PARM : GROUP EQUAL NAME ;
JESLOG_PARM : JESLOG EQUAL (SPIN | NOSPIN | SUPPRESS) ;
JOBRC_PARM : JOBRC EQUAL (MAXRC | LASTRC | (LPAREN STEP COMMA NAME (DOT NAME)? RPAREN)) ;
LINES_PARM : LINES EQUAL NUM_LIT (COMMA (CANCEL | DUMP | WARNING))? ;
MEMLIMIT_PARM : MEMLIMIT EQUAL ((NUM_LIT ('M' | 'G' | 'T' | 'P')) | NOLIMIT) ;
MSGCLASS_PARM : MSGCLASS EQUAL (ALPHA | NUM) ;
MSGLEVEL_PARM : MSGLEVEL EQUAL LPAREN? (ALPHA | NUM) (COMMA (ALPHA | NUM))? RPAREN? ;
NOTIFY_PARM : NOTIFY EQUAL NAME (DOT NAME)? ;
PAGES_PARM : PAGES EQUAL NUM_LIT (COMMA (CANCEL | DUMP | WARNING))? ;
PASSWORD_PARM : PASSWORD EQUAL LPAREN? ALNUMNAT8 (COMMA ALNUMNAT8)? RPAREN? ;
PERFORM_PARM : PERFORM EQUAL NUM_LIT ;
PRTY_PARM : PRTY EQUAL NUM_LIT ;
RD_PARM : RD EQUAL (R | RNC | NR | NC) ;
REGION_PARM : REGION EQUAL NUM_LIT (K | M) ;
RESTART_PARM : RESTART EQUAL (ASTERISK | NAME (DOT NAME)?) (COMMA UNQUOTED_STRING)? ;
SECLABEL_PARM : SECLABEL EQUAL ALNUMNAT8 ;
SCHENV_PARM : SCHENV EQUAL ALNUMNAT16 ;
SYSAFF_PARM : SYSAFF EQUAL HYPHEN? LPAREN? HYPHEN? (ALNUMNAT4 | ANY | IND | ASTERISK) (COMMA HYPHEN? (ALNUMNAT4 | ANY | IND | ASTERISK))* RPAREN? ;
SYSTEM_PARM : SYSTEM EQUAL HYPHEN? LPAREN? HYPHEN? (ALNUMNAT8 | ANY | JGLOBAL | JLOCAL | ASTERISK) (COMMA HYPHEN? (ALNUMNAT8 | ANY | JGLOBAL | JLOCAL | ASTERISK))* RPAREN? ;
TIME_PARM : TIME EQUAL LPAREN? (NOLIMIT | MAXIMUM | FOURTEENFORTY | (NUM_LIT (COMMA NUM_LIT)?)) RPAREN? ;
TYPRUN_PARM : TYPRUN EQUAL (COPY | HOLD | JCLHOLD | SCAN) ;
USER_PARM : USER EQUAL NAME ;
*/


