Hier gibt es die Ordner (bpeltest), (Example1), (Linktest), (Picktest), (ScopeTest), (Iftest), (Invoketest),(throwTest) und (de.uni-stuttgart.iaas.bpel-d.test)

In dem Ordner (bpeltest) sind 4 BPEL-Prozesse enthalten. Dann werden die <assign> Aktivitaet und die <receive> Aktivitaet ueberprueft. 
1. Einfachtest.bpel: In diesem BPEL-Prozess sind die <reply> Aktivitaet, die <assign> Aktivitaet und die <receive> Aktivitaet enthalten.
2. testAss.bpel: In diesem BPEL-Prozess sind die <assign> Aktivitaeten enthalten, die unterschiedliche Attributen enthalten.
3. testEL.bpel: In diesem BPEL-Prozess sind die <assign> Aktivitaeten enthalten, die das <expressionLanguage> Attribut enthalten.
4. testPL.bpel: In diesem BPEL-Prozess sind die <assign> Aktivitaeten enthalten, die das <partnerLink> Attribut enthalten.

In dem Ordner (Example1) enthaelt die MotivatingExample.bpel.
Dieser BPEL-Prozess wurde in der Diplomarbeit [Bre08 S.37] wiedergegeben.

In dem Ordner (Linktest) sind 5 BPEL-Prozesse enthalten. Dann wird der Wert von der mbd, die <repeatUntil> Aktivitaet und  die <while> Aktivitaet ueberprueft. 
1. test1.bpel: In diesem BPEL-Prozess sind drei <assign> Aktivitaeten und zwei Links enthalten: ein Link mit <transitionCondition>, ein Link ohne <transitionCondition>. 
2. test2.bpel: Cross-boundary-Links mit der <if> Aktivitaet.
3. test3.bpel: Cross-boundary-Links mit der <pick> Aktivitaet
4. test4.bpel: die <repeatUntil> Aktivitaet mit der <assign> Aktivitaet.
5. test5.bpel: die <while> Aktivitaet mit Links

In dem Ordner (Picktest) ist die PickProcess.bpel enthalten, dann wird die <pick> Aktivitaet ueberprueft. 

In dem Ordner (ScopeTest) sind 5 BPEL-Prozesse enthalten. Dann werden FH, EH, TH CH und die <forEach> Aktivitaet ueberprueft.
1. CPtest.bpel: In diesem BPEL-Prozess sind das compensationHandler und das faultHandler, die <forEach> Aktivitaet enthalten,  
2. EhTest.bpel: In diesem BPEL-Prozess sind das eventHandler und die <flow> Aktivitaet enthalten.
3. EhTest.bpel: In diesem BPEL-Prozess sind das eventHandler und die <sequence> Aktivitaet enthalten.
4. FhTest.bpel: In diesem BPEL-Prozess ist das faultHandler enthalten.
5. ThTest.bpel: In diesem BPEL-Prozess ist das terminationHandler enthalten.

In dem Ordner (Iftest) ist die DynamicInvokeProcess.bpel enthalten, dann wird die <if> Aktivitaet ueberprueft. 

In dem Ordner (Invoketest) ist die SimpleInvokeProcess.bpel enthalten, dann wird die <invoke> Aktivitaet ueberprueft.

In dem Ordner (throwTest) sind 5 BPEL-Prozesse enthalten. Dann werden die <throw> Aktivitaeten ueberprueft.
1. test1.bpel: In diesem BPEL-Prozess sind die <throw> Aktivitaet, zwei <assign> Aktivitaeten und zwei Links mit <transitionCondition> enthalten.
2. test2.bpel: In diesem BPEL-Prozess ist die <invoke> Aktivitaet enthalten, die in der FH enthalten ist.
3. test3.bpel: eine <throw> Aktivitaet ist in der <if> Aktivitaet enthalten, eine andere <throw> Aktivitaet ist in der <pick> Aktivitaet der onMessager-Zweige enthalten und die <Rethrow> Aktivitaet ist in dem compensationHndler enthalten.
4. test4.bpel: In diesem BPEL-Prozess ist die <throw> Aktivitaet enthalten, drei <assign> Aktivitaeten und drei Links, diese haben keine <transitionCondition>.
5. test5.bpel: Das TH mit dem Compensate, das in der <ForEach> Aktivitaet enthalten ist.  

In dem Ordner (de.uni-stuttgart.iaas.bpel-d.test) wird die joinCondition ueberprueft. Hier wird die JUnit verwendet.

Das Java-Programm und die BPEL-Prozesse wird in der Eclipse SDK Version: 3.4.2 durchgefuehrt. Alle BPEL-Prozesse File sind BPEL 2.0.  
