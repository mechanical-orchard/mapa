000001 ID Division.
000002 Program-ID. testantlr156.
000003 Data Division.
000004 Working-Storage Section.
000005 01  WORK-AREAS.
000006     05  WS-COUNT              PIC S9999 COMP-3 VALUE +0.
000007 Procedure Division.
000008     PERFORM 10 TIMES
000009       ADD 1 TO WS-COUNT
000010       IF WS-COUNT = 3
000011         EXIT PERFORM
000012       END-IF
000013     END-PERFORM
000014
000015     PERFORM 10 TIMES
000016       ADD 1 TO WS-COUNT
000017       IF WS-COUNT = 4
000018         EXIT PERFORM CYCLE
000019       END-IF
000020     END-PERFORM
000021
000022     PERFORM 10 TIMES
000023       ADD 1 TO WS-COUNT
000024       IF WS-COUNT = 13
000025         EXIT METHOD
000026       END-IF
000027     END-PERFORM
000028
000029     EXIT SECTION
000030     EXIT PARAGRAPH
000031
000032     EXIT PROGRAM.
