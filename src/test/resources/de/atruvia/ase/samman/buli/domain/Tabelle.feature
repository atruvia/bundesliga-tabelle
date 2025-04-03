# language: de

Funktionalität: Tabelle erstellen

Szenario: Zwei Mannschaften sind punkt- und torgleich (Beispiel volle Tabelle)

	Gegeben sei ein Spielplan
		| Heim   | Gast   | Ergebnis |
		| Team-A | Team-B |      1:0 |
		| Team-B | Team-A |      1:0 |
		| Team-A | Team-C |      1:0 |
		| Team-B | Team-C |      1:0 |
	Wenn die Tabelle berechnet wird
	Dann ist die Tabelle
		| Platz | Team   | Spiele | Siege | Unentschieden | Niederlagen | Punkte | Tore | Gegentore | Tordifferenz | Tendenz |
		|     1 | Team-A |      3 |     2 |             0 |           1 |      6 |    2 |         1 |            1 | SNS     |
		|     1 | Team-B |      3 |     2 |             0 |           1 |      6 |    2 |         1 |            1 | SSN     |
		|     3 | Team-C |      2 |     0 |             0 |           2 |      0 |    0 |         2 |           -2 | NN      |

Szenario: Zwei Mannschaften sind punkt- und torgleich (Beispiel gezielt nur Team und Platz)

	Gegeben sei ein Spielplan
		| Heim   | Gast   | Ergebnis |
		| Team-A | Team-B |      1:0 |
		| Team-B | Team-A |      1:0 |
		| Team-A | Team-C |      1:0 |
		| Team-B | Team-C |      1:0 |
	Wenn die Tabelle berechnet wird
	Dann ist die Tabelle
		| Team   | Platz |
		| Team-A |     1 |
		| Team-B |     1 |
		| Team-C |     3 |
