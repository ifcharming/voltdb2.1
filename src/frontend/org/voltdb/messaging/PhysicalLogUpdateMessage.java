package org.voltdb.messaging;

import java.io.IOException;

import org.voltdb.logging.Level;
import org.voltdb.logging.VoltLogger;
import org.voltdb.utils.DBBPool;

public class PhysicalLogUpdateMessage extends TransactionInfoBaseMessage {

	private static final VoltLogger hostLog = new VoltLogger("HOST");

	boolean m_isSinglePartition;
	long m_lastSafeTxnID; // this is the largest txn acked by all partitions running the java for it
	int[] m_otherSiteIds;
	byte[] m_payload = null;

	/** Empty constructor for de-serialization */
	PhysicalLogUpdateMessage() {
		super();
	}
	/**
	 * These four args needed for base class
	 * @param initiatorSiteId
	 * @param coordinatorSiteId
	 * @param txnId
	 * @param isReadOnly
	 * @param isSinglePartition
	 * @param lastSafeTxnId
	 * @param otherSiteIds
	 * @param payload
	 */
	public PhysicalLogUpdateMessage(int initiatorSiteId, int coordinatorSiteId,
			long txnId, boolean isReadOnly,
			boolean isSinglePartition, long lastSafeTxnID,
			int[] otherSiteIds, byte[] payload) {
		super(initiatorSiteId, coordinatorSiteId, txnId, isReadOnly);

		m_isSinglePartition = isSinglePartition;
		m_lastSafeTxnID = lastSafeTxnID;
		assert(otherSiteIds != null);
		assert(payload != null);
		m_otherSiteIds = otherSiteIds;
		m_payload = payload;
	}

	@Override
	public boolean isReadOnly() {
		return m_isReadOnly;
	}

	@Override
	public boolean isSinglePartition() {
		return m_isSinglePartition;
	}

	public long getLastSafeTxnId() {
		return m_lastSafeTxnID;
	}

	public int getOtherSiteCount() {
		return m_otherSiteIds.length;
	}
	public int getOtherSiteIds(int index) {
		return m_otherSiteIds[index];
	}

	@Override
	protected void flattenToBuffer(DBBPool pool) throws IOException {
		int msgsize = super.getMessageByteCount();
		// Add the bytes for payload count
		msgsize += 1 + 8 + 2 + 2;

		if (m_otherSiteIds != null) {
			msgsize += 4 * m_otherSiteIds.length;
		}
		if (m_payload != null) {
			msgsize += m_payload.length;
		}

		if (m_buffer == null) {
			m_container = pool.acquire(msgsize + 1 + HEADER_SIZE);
			m_buffer = m_container.b;
		}
		setBufferSize(msgsize + 1, pool);

		m_buffer.position(HEADER_SIZE);
		m_buffer.put(PHYSICAL_LOG_UPDATE_ID);

		super.writeToBuffer();

		hostLog.l7dlog( Level.INFO, "m_payload="+m_payload.toString(), new Object[] { m_initiatorSiteId, m_coordinatorSiteId}, null);

		m_buffer.put(m_isSinglePartition ? (byte) 1 : (byte) 0);
		m_buffer.putLong(m_lastSafeTxnID);

		if (m_otherSiteIds == null) {
			m_buffer.putShort((short)0);
		} else {
			m_buffer.putShort((short) m_otherSiteIds.length);
			for (int i = 0; i < m_otherSiteIds.length; i++) {
				m_buffer.putInt(m_otherSiteIds[i]);
			}
		}

		if (m_payload == null) {
			m_buffer.putShort((short) 0);
		} else {
			m_buffer.putShort((short) m_payload.length);
			for (int i = 0; i < m_payload.length; i++) {
				m_buffer.put(m_payload[i]);
			}
		}
		m_buffer.limit(m_buffer.position());
	}

	@Override
	protected void initFromBuffer() {
		m_buffer.position(HEADER_SIZE + 1); // skip the msg id
		super.readFromBuffer();

		m_isSinglePartition = m_buffer.get() == 1;
		m_lastSafeTxnID = m_buffer.getLong();

		short otherSiteCount = m_buffer.getShort();
		if (otherSiteCount > 0) {
			m_otherSiteIds = new int[otherSiteCount];
			for (int i = 0; i < otherSiteCount; i++) {
				m_otherSiteIds[i] = m_buffer.getInt();
			}
		}

		short payloadCount = m_buffer.getShort();
		if (payloadCount > 0) {
			m_payload = new byte[payloadCount];
			for (int i = 0; i < payloadCount; i++) {
				m_payload[i] = m_buffer.get();
			}
		}
	}

}
