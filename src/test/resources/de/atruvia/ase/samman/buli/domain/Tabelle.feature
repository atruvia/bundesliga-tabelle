# language: de

Funktionalität: Tabelle erstellen

Szenario: Zwei Mannschaften sind punkt- und torgleich (Beispiel volle Tabelle)

	Gegeben sei ein Spielplan
		| Heim   | Gast   | Ergebnis |
		| Team 1 | Team 2 |      1:0 |
		| Team 2 | Team 1 |      1:0 |
		| Team 1 | Team 3 |      1:0 |
		| Team 2 | Team 3 |      1:0 |
	Wenn die Tabelle berechnet wird
	Dann ist die Tabelle
		| Platz| Team   | Spiele | Siege | Unentschieden | Niederlagen | Punkte | Tore | Gegentore | Tordifferenz | Tendenz |
		|    1 | Team 1 |      3 |     2 |             0 |           1 |      6 |    2 |         1 |            1 | SNS     |
		|    1 | Team 2 |      3 |     2 |             0 |           1 |      6 |    2 |         1 |            1 | SSN     |
		|    3 | Team 3 |      2 |     0 |             0 |           2 |      0 |    0 |         2 |           -2 | NN      |

Szenario: Zwei Mannschaften sind punkt- und torgleich (Beispiel gezielt nur Team und Platz)

	Gegeben sei ein Spielplan
		| Heim   | Gast   | Ergebnis |
		| Team 1 | Team 2 |      1:0 |
		| Team 2 | Team 1 |      1:0 |
		| Team 1 | Team 3 |      1:0 |
		| Team 2 | Team 3 |      1:0 |
	Wenn die Tabelle berechnet wird
	Dann ist die Tabelle
		| Team   | Platz|
		| Team 1 |    1 |
		| Team 2 |    1 |
		| Team 3 |    3 |
