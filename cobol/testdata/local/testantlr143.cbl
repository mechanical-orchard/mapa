000001 Identification Division.
000002 Program-ID. testantlr143.
000003 Data Division.
000004 Working-Storage Section.
000005
000006 01  CONSTANTS.
000007     05  MYNAME               PIC X(012) VALUE 'testantlr143'.
000008     >>EVALUATE TRUE
000009     >>WHEN X = 1
000010     05  PGM-0001             PIC X(008) VALUE 'PGMA0001'.
000011     >>WHEN X = 2
000012     05  PGM-0001             PIC X(008) VALUE 'PGMA0002'.
000013     >>WHEN X = 3
000014     05  PGM-0001             PIC X(008) VALUE 'PGMA0003'.
000015     >>WHEN OTHER
000016     05  PGM-0001             PIC X(008) VALUE 'PGMA0009'.
000017     >>END-EVALUATE
000018
000019*
000020
000021 Procedure Division.
000022     DISPLAY MYNAME ' Begin'
000023     CALL PGM-0001
000024
000025     DISPLAY MYNAME ' End'
000026     
000027     GOBACK
000028     .
000029
000030
