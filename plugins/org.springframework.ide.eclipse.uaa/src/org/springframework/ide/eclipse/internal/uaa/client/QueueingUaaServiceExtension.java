/*******************************************************************************
 * Copyright (c) 2011 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.internal.uaa.client;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.prefs.BackingStoreException;

<<<<<<< HEAD
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
=======
>>>>>>> master
import org.springframework.uaa.client.TransmissionEventListener;
import org.springframework.uaa.client.TransmissionService;
import org.springframework.uaa.client.VersionHelper;
import org.springframework.uaa.client.internal.TransmissionAwareUaaServiceImpl;
import org.springframework.uaa.client.protobuf.UaaClient.FeatureUse;
import org.springframework.uaa.client.protobuf.UaaClient.Privacy.PrivacyLevel;
import org.springframework.uaa.client.protobuf.UaaClient.Product;
import org.springframework.uaa.client.protobuf.UaaClient.UaaEnvelope;

/**
 * Extension to the UAA {@link TransmissionAwareUaaServiceImpl} that queues product and feature use 
 * usage records for {@link #BATCH_INTERVAL}
 * @author Christian Dupuis
 * @since 2.6.0
 */
public class QueueingUaaServiceExtension extends TransmissionAwareUaaServiceImpl {

	private static final long BATCH_INTERVAL = 1000L * 60L * 5L; // batch report all registrations every 5mins
	private static final String THREAD_NAME_TEMPLATE = "Event Batch Processing (%s/%s.%s.%s)";
	
	private final TransmissionEventListener eventListener = new QueueFlushingTramissionEventListener();

	private final Thread queueFlushingThread;
	private volatile boolean shouldExit = false;

<<<<<<< HEAD
	private final TransmissionEventListener eventListener = new FlushingTramissionEventListener();
=======
	/** The internal queue of {@link UsageRecord}s that will be stored until processed by {@link QueueFlushingRunnable}. */
	private final Queue<UsageRecord> usageRecords = new ConcurrentLinkedQueue<UsageRecord>();
>>>>>>> master

	public QueueingUaaServiceExtension(TransmissionService transmssionService) {
		super(transmssionService);

		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				stopQueueFlushingThread();
			}
		});

		// Start up scheduling thread
<<<<<<< HEAD
		this.queuedUsageRecordsProcessingThread = new Thread(new QueuedUsageRecordRunnable(), getThreadName(THREAD_NAME_TEMPLATE));
		this.queuedUsageRecordsProcessingThread.setDaemon(true);
		this.queuedUsageRecordsProcessingThread.start();
		
		removeTransmissionEventListener(eventListener);
=======
		this.queueFlushingThread = new Thread(new QueueFlushingRunnable(), getThreadName(THREAD_NAME_TEMPLATE));
		this.queueFlushingThread.setDaemon(true);
		this.queueFlushingThread.start();
		
		addTransmissionEventListener(eventListener);
>>>>>>> master
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public byte[] getFeatureUseData(Product product, FeatureUse featureUse) {
		
		// Work on a local copy
		UsageRecord[] records = usageRecords.toArray(new UsageRecord[0]);
		
		// Iterate over records (newest first) and see if there is a feature registered
		for (int j = records.length - 1; j >= 0; j = j - 1) {
			UsageRecord record = records[j];
			if (record instanceof FeatureUseUsageRecord) {
				Product queuedProduct = ((FeatureUseUsageRecord) record).getProduct();
				FeatureUse queuedFeatureUse = ((FeatureUseUsageRecord) record).getFeatureUse();
				if (queuedProduct.getName().equals(product.getName()) 
						&& queuedProduct.getMajorVersion() == product.getMajorVersion()
						&& queuedProduct.getMinorVersion() == product.getMinorVersion()
						&& queuedProduct.getPatchVersion() == product.getPatchVersion()
						&& queuedProduct.getReleaseQualifier().equals(product.getReleaseQualifier())) {
					
					if (queuedFeatureUse.getName().equals(featureUse.getName()) 
							&& queuedFeatureUse.getMajorVersion() == featureUse.getMajorVersion()
							&& queuedFeatureUse.getMinorVersion() == featureUse.getMinorVersion()
							&& queuedFeatureUse.getPatchVersion() == featureUse.getPatchVersion()
							&& queuedFeatureUse.getReleaseQualifier().equals(featureUse.getReleaseQualifier())) {
						return ((FeatureUseUsageRecord) record).getFeatureData();
					}
				}
			}
		}
		
		return super.getFeatureUseData(product, featureUse);
	}

	public UaaEnvelope getPayload() {
		return createUaaEnvelope();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public byte[] getProductUseData(Product product) {
		
		// Work on a local copy
		UsageRecord[] records = usageRecords.toArray(new UsageRecord[0]);
		
		// Iterate over records (newest first) and see if there is a product registered
		for (int j = records.length - 1; j >= 0; j = j - 1) {
			UsageRecord record = records[j];
			if (record instanceof FeatureUseUsageRecord) {
				Product queuedProduct = ((ProductUsageRecord) record).getProduct();
				if (queuedProduct.getName().equals(product.getName()) 
						&& queuedProduct.getMajorVersion() == product.getMajorVersion()
						&& queuedProduct.getMinorVersion() == product.getMinorVersion()
						&& queuedProduct.getPatchVersion() == product.getPatchVersion()
						&& queuedProduct.getReleaseQualifier().equals(product.getReleaseQualifier())) {
					return ((ProductUsageRecord) record).getProductData();
				}
			}
		}

		return super.getProductUseData(product);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void registerFeatureUsage(Product product, FeatureUse feature) {
		registerFeatureUsage(product, feature, null);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void registerFeatureUsage(Product product, FeatureUse feature, byte[] featureData) {
		queueFeatureUseUsageRecord(product, feature, featureData);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void registerProductUsage(Product product) {
		registerProductUsage(product, null, null);
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void registerProductUsage(Product product, byte[] productData) {
		registerProductUsage(product, productData, null);
	}
	
	/**
	 * {@inheritDoc}
	 */
	@Override
	public void registerProductUsage(Product product, byte[] productData, String project) {
		queueProductUsageRecord(product, productData, project);
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void registerProductUsage(Product product, String project) {
		registerProductUsage(product, null, project);
	}

	/**
	 * {@inheritDoc}
	 */
<<<<<<< HEAD
	public void stop() {
		stopQueuedUsageRecordsProcessingThread();
		removeTransmissionEventListener(eventListener);
	}

	private void queueFeatureUseUsageRecord(Product product, FeatureUse feature, byte[] featureData) {
		queueUsageRecord(new FeatureUseUsageRecord(feature, product, featureData));
	}

	private void queueProductUsageRecord(Product product, byte[] productData, String projectId) {
		queueUsageRecord(new ProductUsageRecord(product, projectId, productData));
=======
	@Override
	public void setPrivacyLevel(PrivacyLevel privacyLevel) {
		super.setPrivacyLevel(privacyLevel);
		
		// In case we change the privacy level flush the cached records
		processQueuedUsageRecords();
>>>>>>> master
	}

	/**
	 * Stops this instance.
	 */
	public void stop() {
		stopQueueFlushingThread();
		removeTransmissionEventListener(eventListener);
	}

	/**
	 * Returns the nicely formatted name for the name of the thread created by this implementation 
	 */
	private String getThreadName(String template) {
		Product uaaProduct = VersionHelper.getUaa();
		return String.format(template, uaaProduct.getName(), uaaProduct.getMajorVersion(), uaaProduct.getMinorVersion(), uaaProduct.getPatchVersion());
	}

	/**
	 * Processes the {@link UsageRecord}s stored in {@link #usageRecords}.
	 */
	private synchronized void processQueuedUsageRecords() {
<<<<<<< HEAD
=======

		// Check if UAA TOU are accepted so that we can store the events
		if (!isUaaTermsOfUseAccepted()) {
			return;
		}
>>>>>>> master
		
		// Work on a local copy
		UsageRecord[] records = usageRecords.toArray(new UsageRecord[0]);
		
		// Only process if we have reported usage records
		if (records != null && records.length > 0) {
			
			// Sync changes to the backend storage into the UAA infrastructure
			try { P.sync();	}
			catch (BackingStoreException e) {}
			
			// Process each usage record
			for (UsageRecord record : records) {
				if (record instanceof ProductUsageRecord) {
<<<<<<< HEAD
					byte[] data = getProductUseData(record.getProduct());
					byte[] newData = ((ProductUsageRecord) record).getProductData(); 
					super.registerProductUsage(record.getProduct(), mergeData(data, newData), ((ProductUsageRecord) record).getProjectId());
				}
				else if (record instanceof FeatureUseUsageRecord) {
					byte[] data = getFeatureUseData(record.getProduct(), ((FeatureUseUsageRecord) record).getFeatureUse());
					byte[] newData = ((FeatureUseUsageRecord) record).getFeatureData(); 
					super.registerFeatureUsage(record.getProduct(), ((FeatureUseUsageRecord) record).getFeatureUse(), mergeData(data, newData));
=======
					super.registerProductUsage(record.getProduct(), ((ProductUsageRecord) record).getProductData(), ((ProductUsageRecord) record).getProjectId());
				}
				else if (record instanceof FeatureUseUsageRecord) {
					super.registerFeatureUsage(record.getProduct(), ((FeatureUseUsageRecord) record).getFeatureUse(), ((FeatureUseUsageRecord) record).getFeatureData());
>>>>>>> master
				}
				
				// Remove processed records
				usageRecords.remove(record);
			}
			
			// Finally flush changes back into the Preferences API
			try { P.flush(); }
			catch (BackingStoreException e) {}
		}
	}

	private void queueFeatureUseUsageRecord(Product product, FeatureUse feature, byte[] featureData) {
		queueUsageRecord(new FeatureUseUsageRecord(feature, product, featureData));
	}

	private void queueProductUsageRecord(Product product, byte[] productData, String projectId) {
		queueUsageRecord(new ProductUsageRecord(product, projectId, productData));
	}
	
	private void queueUsageRecord(UsageRecord record) {
		usageRecords.offer(record);
	}
	
	@SuppressWarnings("unchecked")
	private byte[] mergeData(byte[] existingData, byte[] data) {
		// Quick sanity check to prevent doing too much in case no new data has been presented
		if (data == null || data.length == 0) {
			return existingData;
		}
		
		// Load existing feature data
		JSONObject existingFeatureData = new JSONObject();
		if (existingData != null && existingData.length > 0) {
			Object existingJson = JSONValue.parse(new String(existingData));
			if (existingJson instanceof JSONObject) {
				existingFeatureData.putAll(((JSONObject) existingJson));
			}
		}
		
		// Load new data into JSON object
		Map<String, String> featureData = new JSONObject();
		if (data != null && data.length > 0) {
			Object json = JSONValue.parse(new String(data));
			if (json instanceof JSONObject) {
				featureData.putAll((JSONObject) json);
			}
		}

		// Merge feature data: merge those values whose keys already exist
		featureData = new HashMap<String, String>(featureData);
		for (Map.Entry<String, Object> existingEntry : new HashMap<String, Object>(existingFeatureData).entrySet()) {
			if (featureData.containsKey(existingEntry.getKey())) {
				String newValue = featureData.get(existingEntry.getKey());
				Object existingValue = existingEntry.getValue();
				if (!newValue.equals(existingValue)) {
					if (existingValue instanceof List) {
						List<String> existingValues = (List<String>) existingValue;
						if (!existingValues.contains(newValue)) {
							existingValues.add(newValue);
						}
					}
					else {
						List<String> value = new ArrayList<String>();
						value.add((String) existingValue);
						value.add(featureData.get(existingEntry.getKey()));
						existingFeatureData.put(existingEntry.getKey(), value);
					}
				}
				featureData.remove(existingEntry.getKey());
			}
		}

		// Merge the remaining new values
		existingFeatureData.putAll(featureData);

		try { return existingFeatureData.toJSONString().getBytes("UTF-8"); }
		catch (UnsupportedEncodingException e) {
			return null;
		}
	}
	
	/**
	 * Stop the internal queue processing job.
	 */
	private void stopQueueFlushingThread() {
		shouldExit = true;
		if (queueFlushingThread != null) {
			queueFlushingThread.interrupt();
		}
		processQueuedUsageRecords();
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void loadChangesFromStorage() {
		// Prevent UaaServiceImpl to control sync and flush from Preferences API
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void persistChangesToStorage() {
		// Prevent UaaServiceImpl to control sync and flush from Preferences API		
	}
	
	/**
	 * {@link Runnable} that periodically calls {@link QueueingUaaServiceExtension#processQueuedUsageRecords()}. 
	 */
<<<<<<< HEAD
	class QueuedUsageRecordRunnable implements Runnable {
=======
	class QueueFlushingRunnable implements Runnable {
>>>>>>> master

		/**
		 * {@inheritDoc}
		 */
		public void run() {

			// Wait a defined period; enables testing
			try { Thread.sleep(BATCH_INTERVAL);	}
			catch (InterruptedException e) {}

			while (true) {
				// Test if this thread needs exiting
				if (shouldExit) {
					return;
				}
				
				// Initiate processing of usage records
				processQueuedUsageRecords();
				
				// Eventually wait until next run
				if (!Thread.interrupted() && !shouldExit) {
					try { Thread.sleep(BATCH_INTERVAL);	}
					catch (InterruptedException e) {
						return;
					}
				}
			}
		}
	}
	
<<<<<<< HEAD
	class FlushingTramissionEventListener implements TransmissionEventListener {
=======
	/**
	 * {@link TransmissionEventListener} that calls {@link QueueingUaaServiceExtension#processQueuedUsageRecords()}
	 * on each {@link TransmissionType.UPLOAD} event so that the queue is flushed ahead of each upload. 
	 */
	class QueueFlushingTramissionEventListener implements TransmissionEventListener {

		public void afterTransmission(TransmissionType type, boolean successful) {
			// Intentionally left empty
		}
>>>>>>> master

		public void beforeTransmission(TransmissionType type) {
			if (type == TransmissionType.UPLOAD) {
				processQueuedUsageRecords();
			}
		}
<<<<<<< HEAD

		public void afterTransmission(TransmissionType type, boolean successful) {
			// Intentionally left empty
		}
=======
>>>>>>> master
	}
}
