/*******************************************************************************
 *  Copyright (c) 2012 VMware, Inc.
 *  All rights reserved. This program and the accompanying materials
 *  are made available under the terms of the Eclipse Public License v1.0
 *  which accompanies this distribution, and is available at
 *  http://www.eclipse.org/legal/epl-v10.html
 *
 *  Contributors:
 *      VMware, Inc. - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.config.core.schemas;

/**
 * Batch schema derived from
 * <code>http://www.springframework.org/schema/batch/spring-batch-2.0.xsd</code>
 * @author Leo Dos Santos
 * @author Christian Dupuis
 * @since STS 2.1.0
 * @version Spring Batch 2.0
 */
public class BatchSchemaConstants {

	// URI

	public static String URI = "http://www.springframework.org/schema/batch"; //$NON-NLS-1$

	// Element tags

	public static String ELEM_CHUNK = "chunk"; //$NON-NLS-1$

	public static String ELEM_DECISION = "decision"; //$NON-NLS-1$

	public static String ELEM_END = "end"; //$NON-NLS-1$

	public static String ELEM_FAIL = "fail"; //$NON-NLS-1$

	public static String ELEM_FATAL_EXCEPTION_CLASSES = "fatal-exception-classes"; //$NON-NLS-1$

	public static String ELEM_FLOW = "flow"; //$NON-NLS-1$

	public static String ELEM_JOB = "job"; //$NON-NLS-1$

	public static String ELEM_JOB_LISTENER = "job-listener"; //$NON-NLS-1$

	public static String ELEM_JOB_REPOSITORY = "job-repository"; //$NON-NLS-1$

	public static String ELEM_LISTENER = "listener"; //$NON-NLS-1$

	public static String ELEM_LISTENERS = "listeners"; //$NON-NLS-1$

	public static String ELEM_NEXT = "next"; //$NON-NLS-1$

	public static String ELEM_NO_ROLLBACK_EXCEPTION_CLASSES = "no-rollback-exception-classes"; //$NON-NLS-1$

	public static String ELEM_RETRY_LISTENERS = "retry-listeners"; //$NON-NLS-1$

	public static String ELEM_RETRYABLE_EXCEPTION_CLASSES = "retryable-exception-classes"; //$NON-NLS-1$

	public static String ELEM_SKIPPABLE_EXCEPTION_CLASSES = "skippable-exception-classes"; //$NON-NLS-1$

	public static String ELEM_SPLIT = "split"; //$NON-NLS-1$

	public static String ELEM_STEP = "step"; //$NON-NLS-1$

	public static String ELEM_STEP_LISTENER = "step-listener"; //$NON-NLS-1$

	public static String ELEM_STOP = "stop"; //$NON-NLS-1$

	public static String ELEM_STREAM = "stream"; //$NON-NLS-1$

	public static String ELEM_STREAMS = "streams"; //$NON-NLS-1$

	public static String ELEM_TASKLET = "tasklet"; //$NON-NLS-1$

	public static String ELEM_TRANSACTION_ATTRIBUTES = "transaction-attributes"; //$NON-NLS-1$

	// Attribute tags

	public static String ATTR_ABSTRACT = "abstract"; //$NON-NLS-1$

	public static String ATTR_AFTER_CHUNK_METHOD = "after-chunk-method"; //$NON-NLS-1$

	public static String ATTR_AFTER_JOB_METHOD = "after-job-method"; //$NON-NLS-1$

	public static String ATTR_AFTER_PROCESS_METHOD = "after-process-method"; //$NON-NLS-1$

	public static String ATTR_AFTER_READ_METHOD = "after-read-method"; //$NON-NLS-1$

	public static String ATTR_AFTER_STEP_METHOD = "after-step-method"; //$NON-NLS-1$

	public static String ATTR_AFTER_WRITE_METHOD = "after-write-method"; //$NON-NLS-1$

	public static String ATTR_ALLOW_START_IF_COMPLETE = "allow-start-if-complete"; //$NON-NLS-1$

	public static String ATTR_BEFORE_CHUNK_METHOD = "before-chunk-method"; //$NON-NLS-1$

	public static String ATTR_BEFORE_JOB_METHOD = "before-job-method"; //$NON-NLS-1$

	public static String ATTR_BEFORE_PROCESS_METHOD = "before-process-method"; //$NON-NLS-1$

	public static String ATTR_BEFORE_READ_METHOD = "before-read-method"; //$NON-NLS-1$

	public static String ATTR_BEFORE_STEP_METHOD = "before-step-method"; //$NON-NLS-1$

	public static String ATTR_BEFORE_WRITE_METHOD = "before-write-method"; //$NON-NLS-1$

	public static String ATTR_CACHE_CAPACITY = "cache-capacity"; //$NON-NLS-1$

	public static String ATTR_CHUNK_COMPLETION_POLICY = "chunk-completion-policy"; //$NON-NLS-1$

	public static String ATTR_CLASS = "class"; //$NON-NLS-1$

	public static String ATTR_COMMIT_INTERVAL = "commit-interval"; //$NON-NLS-1$

	public static String ATTR_DATA_SOURCE = "data-source"; //$NON-NLS-1$

	public static String ATTR_DECIDER = "decider"; //$NON-NLS-1$

	public static String ATTR_EXIT_CODE = "exit-code"; //$NON-NLS-1$

	public static String ATTR_ID = "id"; //$NON-NLS-1$

	public static String ATTR_INCREMENTER = "incrementer"; //$NON-NLS-1$

	public static String ATTR_IS_READER_TRANSACTIONAL_QUEUE = "is-reader-transactional-queue"; //$NON-NLS-1$

	public static String ATTR_ISOLATION = "isolation"; //$NON-NLS-1$

	public static String ATTR_ISOLATION_LEVEL_FOR_CREATE = "isolation-level-for-create"; //$NON-NLS-1$

	public static String ATTR_JOB_REPOSITORY = "job-repository"; //$NON-NLS-1$

	public static String ATTR_MERGE = "merge"; //$NON-NLS-1$

	public static String ATTR_NEXT = "next"; //$NON-NLS-1$

	public static String ATTR_ON = "on"; //$NON-NLS-1$

	public static String ATTR_ON_PROCESS_ERROR_METHOD = "on-process-error-method"; //$NON-NLS-1$

	public static String ATTR_ON_READ_ERROR_METHOD = "on-read-error-method"; //$NON-NLS-1$

	public static String ATTR_ON_SKIP_IN_PROCESS_METHOD = "on-skip-in-process-method"; //$NON-NLS-1$

	public static String ATTR_ON_SKIP_IN_READ_METHOD = "on-skip-in-read-method"; //$NON-NLS-1$

	public static String ATTR_ON_SKIP_IN_WRITE_METHOD = "on-skip-in-write-method"; //$NON-NLS-1$

	public static String ATTR_ON_WRITE_ERROR_METHOD = "on-write-error-method"; //$NON-NLS-1$

	public static String ATTR_PARENT = "parent"; //$NON-NLS-1$

	public static String ATTR_PROCESSOR = "processor"; //$NON-NLS-1$

	public static String ATTR_PROPAGATION = "propagation"; //$NON-NLS-1$

	public static String ATTR_READER = "reader"; //$NON-NLS-1$

	public static String ATTR_REF = "ref"; //$NON-NLS-1$

	public static String ATTR_RESTART = "restart"; //$NON-NLS-1$

	public static String ATTR_RESTARTABLE = "restartable"; //$NON-NLS-1$

	public static String ATTR_RETRY_LIMIT = "retry-limit"; //$NON-NLS-1$

	public static String ATTR_SKIP_LIMIT = "skip-limit"; //$NON-NLS-1$

	public static String ATTR_START_LIMIT = "start-limit"; //$NON-NLS-1$

	public static String ATTR_TABLE_PREFIX = "table-prefix"; //$NON-NLS-1$

	public static String ATTR_TASK_EXECUTOR = "task-executor"; //$NON-NLS-1$

	public static String ATTR_TIMEOUT = "timeout"; //$NON-NLS-1$

	public static String ATTR_TO = "to"; //$NON-NLS-1$

	public static String ATTR_TRANSACTION_MANAGER = "transaction-manager"; //$NON-NLS-1$

	public static String ATTR_WRITER = "writer"; //$NON-NLS-1$

}
