
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
/*
 * @author Kaitlin Duck Sherwood
 */

package com.test;

import org.springframework.beans.factory.annotation.Autowired;

public class AddAutowiredConstructorTest {

	public int publicInt;

	private int privateInt = 0;

	public final int publicFinalInt = 0;

	private final int privateFinalInt;

	public final String privateFinalString;

	private static final String staticString = "static string";

	class NoFinalNoConstructor {
		public int publicInt0;

		private final int privateInt0 = 0;

		public String publicString0;

		public String privateString0;

	}



	class FinalsNoConstructor {

		public int publicInt1;

		private int privateInt1 = 0;

		public final int publicFinalInt1 = 0;

		public final String publicFinalString1;

		public final String privateFinalString1;
		
		private final int privateFinalInt1;


		private static final String staticString1 = "static string";
	}

	class FinalsDefaultConstructor {

		public int publicInt2;

		private int privateInt2 = 0;

		public final int publicFinalInt2 = 0;

		private final int privateFinalInt2;

		public final String publicFinalString2;

		public final String privateFinalString2;

		private static final String staticString2 = "static string";
		FinalsDefaultConstructor() {
			System.out.println("This is a boring line");
		}
	}
	

	class FinalsConstructor {
		
		public FinalsConstructor(int publicInt, int privateInt, int privateFinalInt, String publicFinalString,
				String privateFinalString) {
			super();
			this.publicInt3 = publicInt;
			this.privateInt3 = privateInt;
			this.privateFinalInt3 = privateFinalInt;
			this.publicFinalString3 = publicFinalString;
			this.privateFinalString3 = privateFinalString;
		}

		public int publicInt3;

		private int privateInt3 = 0;

		public final int publicFinalInt3 = 0;

		private final int privateFinalInt3;

		public final String publicFinalString3;

		public final String privateFinalString3;

		private static final String staticString3 = "static string";

	}
	

	class NoFinalsConstructor {

		public NoFinalsConstructor(int publicInt, int privateInt, String publicFinalString, String privateFinalString) {
			super();
			this.publicInt4 = publicInt;
			this.privateInt4 = privateInt;
			this.publicFinalString4 = publicFinalString;
			this.privateFinalString4 = privateFinalString;
		}

		public int publicInt4;

		private int privateInt4 = 0;

		public String publicFinalString4;

		public String privateFinalString4;

		private static final String staticString4 = "static string";

	}
	
	class ExtendingFinalsNoConstructor extends NonExistantType {

		public int publicInt5;

		private int privateInt5 = 0;

		public final int publicFinalInt5 = 0;

		private final int privateFinalInt5;

		public final String publicFinalString5;

		public final String privateFinalString5;

		private static final String staticString5 = "static string";
	}

	class ImplementingFinalsNoConstructor implements NonExistantInterface {

		public int publicInt6;

		private int privateInt6 = 0;

		public final int publicFinalInt6 = 0;

		private final int privateFinalInt6;

		public final String publicFinalString6;

		public final String privateFinalString6;

		private static final String staticString6 = "static string";
	}

	class NoUninitializedFinalsDefaultConstructor {

		public int publicInt7;

		private int privateInt7 = 0;

		public final int publicFinalInt7 = 0;

		private final int privateFinalInt7 = 1;

		public final String publicFinalString7 = "public final";

		public final String privateFinalString7 = "private final";

		private static final String staticString7 = "static string";

		NoUninitializedFinalsDefaultConstructor() {
			System.out.println("This is a boring line");
		}
	}

}

	

	
