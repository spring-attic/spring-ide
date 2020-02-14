package org.springframework.ide.eclipse.maven.pom;

import org.xml.sax.Attributes;

/**
 * An Attributes implementation that can perform more operations
 * than the attribute list helper supplied with the standard SAX2
 * distribution.
 */
public class AttributesImpl implements Attributes {

	/** Head node. */
	private ListNode fHead;

	/** Tail node. */
	private ListNode fTail;

	/** Length. */
	private int fLength;


	/* Returns the number of attributes. */
	@Override
	public int getLength() {
		return fLength;
	}

	/* Returns the index of the specified attribute. */
	@Override
	public int getIndex(String raw) {
		ListNode place= fHead;
		int index= 0;
		while (place != null) {
			if (place.raw.equals(raw)) {
				return index;
			}
			index++;
			place= place.next;
		}
		return -1;
	}

	/* Returns the index of the specified attribute. */
	@Override
	public int getIndex(String uri, String local) {
		ListNode place= fHead;
		int index= 0;
		while (place != null) {
			if (place.uri.equals(uri) && place.local.equals(local)) {
				return index;
			}
			index++;
			place= place.next;
		}
		return -1;
	}

	/* Returns the attribute URI by index. */
	@Override
	public String getURI(int index) {

		ListNode node= getListNodeAt(index);
		return node != null ? node.uri : null;
	}

	/* Returns the attribute local name by index. */
	@Override
	public String getLocalName(int index) {

		ListNode node= getListNodeAt(index);
		return node != null ? node.local : null;
	}

	/* Returns the attribute raw name by index. */
	@Override
	public String getQName(int index) {

		ListNode node= getListNodeAt(index);
		return node != null ? node.raw : null;

	}

	/* Returns the attribute type by index. */
	@Override
	public String getType(int index) {

		ListNode node= getListNodeAt(index);
		return (node != null) ? node.type : null;
	}

	/* Returns the attribute type by uri and local. */
	@Override
	public String getType(String uri, String local) {

		ListNode node= getListNode(uri, local);
		return (node != null) ? node.type : null;

	}

	/* Returns the attribute type by raw name. */
	@Override
	public String getType(String raw) {

		ListNode node= getListNode(raw);
		return (node != null) ? node.type : null;
	}

	/* Returns the attribute value by index. */
	@Override
	public String getValue(int index) {

		ListNode node= getListNodeAt(index);
		return (node != null) ? node.value : null;
	}

	/* Returns the attribute value by uri and local. */
	@Override
	public String getValue(String uri, String local) {

		ListNode node= getListNode(uri, local);
		return (node != null) ? node.value : null;
	}

	/* Returns the attribute value by raw name. */
	@Override
	public String getValue(String raw) {

		ListNode node= getListNode(raw);
		return (node != null) ? node.value : null;
	}

	/* Adds an attribute. */
	public void addAttribute(String raw, String type, String value) {
		addAttribute(null, null, raw, type, value);
	}

	/* Adds an attribute. */
	public void addAttribute(
		String uri,
		String local,
		String raw,
		String type,
		String value) {

		ListNode node= new ListNode(uri, local, raw, type, value);
		if (fLength == 0) {
			fHead= node;
		} else {
			fTail.next= node;
		}
		fTail= node;
		fLength++;
	}

	/* Inserts an attribute. */
	public void insertAttributeAt(
		int index,
		String raw,
		String type,
		String value) {
		insertAttributeAt(index, null, null, raw, type, value);
	}

	/* Inserts an attribute. */
	public void insertAttributeAt(
		int index,
		String uri,
		String local,
		String raw,
		String type,
		String value) {

		// if list is empty, add attribute
		if (fLength == 0 || index >= fLength) {
			addAttribute(uri, local, raw, type, value);
			return;
		}

		// insert at beginning of list
		ListNode node= new ListNode(uri, local, raw, type, value);
		if (index < 1) {
			node.next= fHead;
			fHead= node;
		} else {
			ListNode prev= getListNodeAt(index - 1);
			node.next= prev.next;
			prev.next= node;
		}
		fLength++;
	}

	/* Removes an attribute. */
	public void removeAttributeAt(int index) {

		if (fLength == 0)
			return;

		if (index == 0) {
			fHead= fHead.next;
			if (fHead == null) {
				fTail= null;
			}
			fLength--;
		} else {
			ListNode prev= getListNodeAt(index - 1);
			ListNode node= getListNodeAt(index);
			if (node != null) {
				prev.next= node.next;
				if (node == fTail) {
					fTail= prev;
				}
				fLength--;
			}
		}
	}

	/* Removes the specified attribute. */
	public void removeAttribute(String raw) {
		removeAttributeAt(getIndex(raw));
	}

	/* Removes the specified attribute. */
	public void removeAttribute(String uri, String local) {
		removeAttributeAt(getIndex(uri, local));
	}

	/* Returns the node at the specified index. */
	private ListNode getListNodeAt(int i) {

		for (ListNode place= fHead; place != null; place= place.next) {
			if (--i == -1) {
				return place;
			}
		}
		return null;
	}

	/* Returns the first node with the specified uri and local. */
	public ListNode getListNode(String uri, String local) {

		if (uri != null && local != null) {
			ListNode place= fHead;
			while (place != null) {
				if (place.uri != null
					&& place.local != null
					&& place.uri.equals(uri)
					&& place.local.equals(local)) {
					return place;
				}
				place= place.next;
			}
		}
		return null;
	}

	/* Returns the first node with the specified raw name. */
	private ListNode getListNode(String raw) {

		if (raw != null) {
			for (ListNode place= fHead; place != null; place= place.next) {
				if (place.raw != null && place.raw.equals(raw)) {
					return place;
				}
			}
		}

		return null;
	}

	/* Returns a string representation of this object. */
	@Override
	public String toString() {
		StringBuilder str= new StringBuilder();

		str.append('[');
		str.append("len="); //$NON-NLS-1$
		str.append(fLength);
		str.append(", {"); //$NON-NLS-1$
		for (ListNode place= fHead; place != null; place= place.next) {
			str.append(place.toString());
			if (place.next != null) {
				str.append(", "); //$NON-NLS-1$
			}
		}
		str.append("}]"); //$NON-NLS-1$

		return str.toString();
	}

	/*
	 * An attribute node.
	 */
	static class ListNode {

		/** Attribute uri. */
		public String uri;

		/** Attribute local. */
		public String local;

		/** Attribute raw. */
		public String raw;

		/** Attribute type. */
		public String type;

		/** Attribute value. */
		public String value;

		/** Next node. */
		public ListNode next;

		/* Constructs a list node. */
		public ListNode(
			String uri0,
			String local0,
			String raw0,
			String type0,
			String value0) {

			this.uri= uri0;
			this.local= local0;
			this.raw= raw0;
			this.type= type0;
			this.value= value0;

		}

		/* Returns string representation of this object. */
		@Override
		public String toString() {
			return raw != null ? raw : local;
		}
	}
}
