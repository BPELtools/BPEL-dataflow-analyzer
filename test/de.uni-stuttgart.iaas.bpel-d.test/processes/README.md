Alle BPEL-Prozesse sind WS-BPEL 2.0.

## AssignTest

4 BPEL-Prozesse. Es wird die `<assign>` Aktivität und die `<receive>` Aktivität überprüft.

1. Einfachtest.bpel: In diesem BPEL-Prozess sind die `<reply>` Aktivität, die `<assign>` Aktivität und die `<receive>` Aktivität enthalten.
2. testAss.bpel: In diesem BPEL-Prozess sind die `<assign>` Aktivitäten enthalten, die unterschiedliche Attributen enthalten.
3. testEL.bpel: In diesem BPEL-Prozess sind die `<assign>` Aktivitäten enthalten, die das `<expressionLanguage>` Attribut enthalten.
4. testPL.bpel: In diesem BPEL-Prozess sind die `<assign>` Aktivitäten enthalten, die das `<partnerLink>` Attribut enthalten.

## Example1
`MotivatingExample.bpel`.
Dieser BPEL-Prozess wurde in der Diplomarbeit [Bre08 S.37] wiedergegeben.

## LinkTest
5 BPEL-Prozesse enthalten. Dann wird der Wert von der mbd, die `<repeatUntil>` Aktivität und  die `<while>` Aktivität überprüft.

1. test1.bpel: In diesem BPEL-Prozess sind drei `<assign>` Aktivitäten und zwei Links enthalten: ein Link mit `<transitionCondition>`, ein Link ohne `<transitionCondition>`.
2. test2.bpel: Cross-boundary-Links mit der `<if>` Aktivität.
3. test3.bpel: Cross-boundary-Links mit der `<pick>` Aktivität
4. test4.bpel: die `<repeatUntil>` Aktivität mit der `<assign>` Aktivität.
5. test5.bpel: die `<while>` Aktivität mit Links

## Picktest
`PickProcess.bpel`: `<pick>` Aktivität überprüft.

## ScopeTest
5 BPEL-Prozesse enthalten. Es werden FH, EH, TH CH und die `<forEach>` Aktivität überprüft.

1. CPtest.bpel: In diesem BPEL-Prozess sind das compensationHandler und das faultHandler, die `<forEach>` Aktivität enthalten.
2. EhTest.bpel: In diesem BPEL-Prozess sind das eventHandler und die `<flow>` Aktivität enthalten.
3. EhTest.bpel: In diesem BPEL-Prozess sind das eventHandler und die `<sequence>` Aktivität enthalten.
4. FhTest.bpel: In diesem BPEL-Prozess ist das faultHandler enthalten.
5. ThTest.bpel: In diesem BPEL-Prozess ist das terminationHandler enthalten.

## IfTest
`DynamicInvokeProcess.bpel`: Die `<if>` Aktivität wird überprüft.

## InvokeTest
`SimpleInvokeProcess.bpel`: Die `<invoke>` Aktivität wird überprüft.

## ThrowTest

In dem Ordner (throwTest) sind 5 BPEL-Prozesse enthalten. Dann werden die `<throw>` Aktivitäten überprüft.
1. test1.bpel: In diesem BPEL-Prozess sind die `<throw>` Aktivität, zwei `<assign>` Aktivitäten und zwei Links mit `<transitionCondition>` enthalten.
2. test2.bpel: In diesem BPEL-Prozess ist die `<invoke>` Aktivität enthalten, die in der FH enthalten ist.
3. test3.bpel: eine `<throw>` Aktivität ist in der `<if>` Aktivität enthalten, eine andere `<throw>` Aktivität ist in der `<pick>` Aktivität der onMessager-Zweige enthalten und die `<Rethrow>` Aktivität ist in dem compensationHndler enthalten.
4. test4.bpel: In diesem BPEL-Prozess ist die `<throw>` Aktivität enthalten, drei `<assign>` Aktivitäten und drei Links, diese haben keine `<transitionCondition>`.
5. test5.bpel: Das TH mit dem Compensate, das in der `<ForEach>` Aktivität enthalten ist.
