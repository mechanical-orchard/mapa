000001 PROCESS DEFINE(X=2)
000002 Identification Division.
000003 Program-ID. testantlr142.
000004 Data Division.
000005 Working-Storage Section.
000006
000007 01  CONSTANTS.
000008     05  MYNAME               PIC X(012) VALUE 'testantlr142'.
000009     >>EVALUATE TRUE
000010     >>WHEN X = 1
000011     05  PGM-0001             PIC X(008) VALUE 'PGMA0001'.
000012     >>WHEN X = 2
000013     05  PGM-0001             PIC X(008) VALUE 'PGMA0002'.
000014     >>WHEN X = 3
000015     05  PGM-0001             PIC X(008) VALUE 'PGMA0003'.
000016     >>WHEN OTHER
000017     05  PGM-0001             PIC X(008) VALUE 'PGMA0009'.
000018     >>END-EVALUATE
000019
000020*
000021
000022 Procedure Division.
000023     DISPLAY MYNAME ' Begin'
000024     CALL PGM-0001
000025
000026     DISPLAY MYNAME ' End'
000027     
000028     GOBACK
000029     .
000030
000031
