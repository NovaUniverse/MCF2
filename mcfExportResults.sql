USE mcf_results;

DROP PROCEDURE IF EXISTS mcfExportResult;

DELIMITER //
CREATE PROCEDURE mcfExportResult(
	IN week_number INT
)

BEGIN
	DECLARE EXIT HANDLER FOR 1062 
	BEGIN
		SELECT "----- Failed -----";
		ROLLBACK;
	END;

	START TRANSACTION;

	CREATE TEMPORARY TABLE player_archive
		(SELECT p.uuid, p.username, p.team_number, p.score, p.kills, week_number AS week
		FROM mcf_mcf.players AS p WHERE p.team_number > 0);

	CREATE TEMPORARY TABLE team_archive
		SELECT t.team_number, t.score, week_number AS week
		FROM mcf_mcf.teams AS t;

	INSERT INTO mcf_results.weeks (week) VALUES (week_number);
	
	INSERT INTO mcf_results.team_result (week, team_number, score)
		SELECT week, team_number, score FROM team_archive;

	INSERT INTO mcf_results.player_result (week, uuid, team_number, score, kills)
		SELECT week, uuid, team_number, score, kills FROM player_archive;

	SELECT "----- output -----";
	SELECT * FROM mcf_results.weeks;
	SELECT * FROM mcf_results.team_result;
	SELECT * FROM mcf_results.player_result;

	COMMIT;
END //
DELIMITER ;