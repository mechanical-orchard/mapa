//AEIOU JOB
//*
//STEP001  EXEC PGM=IEFBR14
//STEP002  EXEC PGM=IEFBR14,PARM=X,CCSID=1
//STEP002A EXEC PGM=IEFBR14,PARM=XX,CCSID=11
//STEP002B EXEC PGM=IEFBR14,PARM=1,CCSID=114
//STEP002C EXEC PGM=IEFBR14,PARM=12,CCSID=1140
//STEP002D EXEC PGM=IEFBR14,PARM=#,CCSID=11405
//STEP002E EXEC PGM=IEFBR14,PARM=#$,CCSID=&CCSID
//STEP002F EXEC PGM=IEFBR14,PARM=X1,CCSID=&CCSID1&CCSID2
//STEP003  EXEC PGM=IEFBR14,PARM='X',CCSID=37
//STEP004  EXEC PGM=IEFBR14,PARM='A1@/STGRPT(ON)',CCSID=37
//STEP005  EXEC PGM=IEFBR14,PARM=&A,CCSID=37
//STEP006  EXEC PGM=IEFBR14,PARM=&AB,CCSID=37
//STEP007  EXEC PGM=IEFBR14,PARM=&A.1,CCSID=37
//STEP008  EXEC PGM=IEFBR14,PARM=ZZ&A,CCSID=37
//STEP009  EXEC PGM=IEFBR14,PARM=ZZ&A.1,CCSID=37
//STEP010  EXEC PGM=IEFBR14,PARM='AND A ONE AND A TWO AND
//       A THREE AND A FOUR',CCSID=37
//STEP010A EXEC PGM=IEFBR14,PARM='AND A ONE AND A TWO AND
//       A THREE AND A FOUR',
// CCSID=37
//STEP011  EXEC PGM=IEFBR14,PARM=(A,B,C,D,E,F,G),CCSID=37
//STEP012  EXEC PGM=IEFBR14,PARM=(A,              PARM 1
//  B,                                            PARM 2
//    C,                                          PARM 3
// D,                                             PARM 4
//         E,                                     PARM 5
//      F,                                        PARM 6
//        G),CCSID=37                             PARM 7
//STEP013  EXEC PGM=IEFBR14,
// PARM=(A,
// B,
// C,
// D,
// E,F,
//         G),CCSID=37
//STEP013A EXEC PGM=IEFBR14,
// PARM=(A,
// B,
// C,
// D,
// E,F,
//         G),
//         CCSID=37
//STEP014  EXEC PGM=IEFBR14,PARM=('ABC',&A,7,'BLAH
//  BLAH BLAH'),CCSID=37
//STEP015  EXEC PGM=IEFBR14,PARM=X,CCSID=37 moo