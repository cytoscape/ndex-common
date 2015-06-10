/**
 *   Copyright (c) 2013, 2015
 *  	The Regents of the University of California
 *  	The Cytoscape Consortium
 *
 *   Permission to use, copy, modify, and distribute this software for any
 *   purpose with or without fee is hereby granted, provided that the above
 *   copyright notice and this permission notice appear in all copies.
 *
 *   THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES
 *   WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF
 *   MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR
 *   ANY SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES
 *   WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN
 *   ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF
 *   OR IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
 */
package org.ndexbio.task;

import java.util.concurrent.TimeUnit;

import org.ndexbio.common.persistence.orientdb.NdexTaskService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.util.concurrent.AbstractScheduledService;

/*
 * Represents a scheduled task that runs  on a periodic basis to scan for
 * database Task entries that have been marked for deletion.
 * The scheduler method determines the frequency of invocation
 * This service is invoked by registering an instance of this class with a Google
 * Service Manager
 */

public class TaskDeletionService extends AbstractScheduledService {
	
	private static final Logger logger = LoggerFactory.getLogger(TaskDeletionService.class);
	private  NdexTaskService ndexService; 
	
	protected void startup() {
		logger.info("TaskDeletionService started");
		ndexService = new NdexTaskService();
	}
	
	/*
	 * This task should run on a continuous basis so stopping it is an error
	 */
	protected void shutdown() {
		logger.error("TaskDeletionService stopped");
	}

	/*
	 * the runOneIteration method is what the ServiceManager invokes and represents the
	 * work of the Service
	 * the runOneIteration really means one iteration per time interval
	 * scan the Tasks for those marked for deletion and remove them from the database
	 */
	@Override
	protected void runOneIteration() throws Exception {
		this.ndexService.deleteTasksQueuedForDeletion();

	}
	

	/*
	 * schedule a scan for every minute
	 * TODO: make the time interval a property
	 */
	@Override
	protected Scheduler scheduler() {
		return Scheduler.newFixedRateSchedule(0, 1, TimeUnit.MINUTES);
	}

}
