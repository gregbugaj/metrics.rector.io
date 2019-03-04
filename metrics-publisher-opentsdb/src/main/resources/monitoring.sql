SELECT DISTINCT(probe) FROM message where application = 'ThreadPool_default' 


SELECT application, probe FROM message 
GROUP BY application, probe

DELETE FROM message
EXPLAIN ANALYZE
	SELECT * FROM message WHERE application = 'ThreadPool_default' AND probe = 'runningTaskCount' AND logdate::date = now()::date ORDER BY ID DESC

	
	SELECT * FROM message WHERE metric_type = 'COUNTER'   ORDER By Application 

	SELECT * FROM message WHERE  probe = 'failedCommandCount' ORDER By Application 


	SELECT * FROM message WHERE m = 'failedCommandCount'

	SELECT * FROM message

--DELETE FROM message
EXPLAIN ANALYZE 
	SELECT COUNT(1) FROM message where Application = 'ThreadPool_default' AND  metric_type ='COUNTER' AND probe IN ( 'completedTaskCount', 'currentQueueSize')


-- Get unique application names
SELECT DISTINCT(split_part(Application,'_', 2)) FROM message where Application like('ThreadPool_%')