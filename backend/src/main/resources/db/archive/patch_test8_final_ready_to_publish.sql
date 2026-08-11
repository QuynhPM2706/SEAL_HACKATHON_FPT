-- Patch Test 8 to: Final scored + ranked, NOT published / awarded / COMPLETED.
-- Goal: QA Publish Final → Assign Awards → Complete Event → Feedback.
-- Event: FE080100-EEEE-4EEE-8EEE-000000000001 (Test 8 - Completed (Feedback Ready))
--
-- Run:
--   sqlcmd -S localhost -U sa -P <pwd> -C -d SEAL -f 65001 -I -i patch_test8_final_ready_to_publish.sql

SET NOCOUNT ON;
SET XACT_ABORT ON;
BEGIN TRANSACTION;

DECLARE @eventId UNIQUEIDENTIFIER = 'FE080100-EEEE-4EEE-8EEE-000000000001';
DECLARE @prelimRound UNIQUEIDENTIFIER = 'FE080300-EEEE-4EEE-8EEE-000000000001';
DECLARE @finalRound UNIQUEIDENTIFIER = 'FE080300-EEEE-4EEE-8EEE-000000000002';

IF NOT EXISTS (SELECT 1 FROM hackathon_events WHERE id = @eventId)
BEGIN
  RAISERROR('Test 8 event not found. Run seed_feature_demo_pack.sql first.', 16, 1);
  ROLLBACK;
  RETURN;
END

-- Staff phase: scoring Final (sticky SCORING — will not auto-complete by endDate)
UPDATE hackathon_events
SET status = N'SCORING',
    leaderboard_public = 0,
    name = N'Test 8 - Final Ready to Publish',
    updated_at = SYSDATETIME()
WHERE id = @eventId;

-- Keep Preliminary publish (historical). Unpublish Final so Publish Flow step is active.
DELETE FROM published_results WHERE round_id = @finalRound;

-- Clear awards / certificates / feedback so Assign Awards + Feedback can be re-tested
DELETE FROM team_awards WHERE event_id = @eventId;

IF OBJECT_ID(N'dbo.participation_certificates', N'U') IS NOT NULL
  DELETE FROM participation_certificates WHERE event_id = @eventId;

DELETE FROM participant_feedbacks WHERE event_id = @eventId;

-- Ensure Final judge scores are COMPLETED (not LOCKED) so Lock & Recalculate is still testable
UPDATE judge_scores
SET status = N'COMPLETED'
WHERE round_id = @finalRound
  AND status = N'LOCKED';

COMMIT TRANSACTION;

PRINT '=== Test 8 patched: Final scored, unpublished, SCORING ===';
PRINT 'EventId: FE080100-EEEE-4EEE-8EEE-000000000001';
PRINT 'Coord:   test.coord@fpt.edu.vn / Demo@123456';
PRINT 'Student: test.fb.s01@fpt.edu.vn / Demo@123456 (Alpha #1 Final)';
PRINT 'Flow:    LiveScore Finals → Lock → Publish → Assign Awards → Event Phase Complete → Feedback';
