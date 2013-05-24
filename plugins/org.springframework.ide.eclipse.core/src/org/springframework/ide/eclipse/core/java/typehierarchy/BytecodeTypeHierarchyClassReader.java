/*******************************************************************************
 * Copyright (c) 2013 Spring IDE Developers
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Spring IDE Developers - initial API and implementation
 *******************************************************************************/
package org.springframework.ide.eclipse.core.java.typehierarchy;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;

import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.internal.compiler.classfmt.ClassFileConstants;

/**
 * @author Martin Lippert
 * @since 3.3.0
 */
@SuppressWarnings("restriction")
public class BytecodeTypeHierarchyClassReader implements TypeHierarchyClassReader {
	
	private ClasspathElement[] paths;

	public BytecodeTypeHierarchyClassReader(ClasspathElement[] locations) {
		this.paths = locations;
	}

	public TypeHierarchyElement readTypeHierarchyInformation(char[] fullyQualifiedClassName, IProject project) {
		String classFileName = new String(fullyQualifiedClassName) + ".class";

		for (int i = 0; i < paths.length; i++) {
			InputStream stream = null;
			try {
				stream = paths[i].getStream(classFileName);
				if (stream != null) {
					return readTypeHierarchy(stream);
				}
			} catch (Exception e) {
			} finally {
				if (stream != null) {
					try {
						stream.close();
					} catch (IOException e) {
					}
				}
			}
		}
		
//		ClassLoader classLoader = JdtUtils.getClassLoader(project, null);
//		InputStream stream = classLoader.getResourceAsStream(classFileName);
//		if (stream != null) {
//			TypeHierarchyElement result = readTypeHierarchy(stream);
//			return result;
//		}
		return null;
	}

	private TypeHierarchyElement readTypeHierarchy(InputStream stream) {
		try {
			DataInputStream dis = new DataInputStream(new BufferedInputStream(stream));
			int magic = dis.readInt(); // magic 0xCAFEBABE
			if (magic != 0xCAFEBABE) {
				throw new IllegalStateException("not bytecode, magic was 0x" + Integer.toString(magic, 16));
			}
			dis.skip(4);
			
			int constantPoolCount = dis.readShort();
			Object[] constantPoolData = new Object[constantPoolCount];
			for (int i = 1; i < constantPoolCount; i++) {
				int tag = dis.readByte();
				switch (tag) {
					case ClassFileConstants.Utf8Tag :
						constantPoolData[i] = dis.readUTF();
						break;
					case ClassFileConstants.IntegerTag :
						dis.skip(4);
						break;
					case ClassFileConstants.FloatTag :
						dis.skip(4);
						break;
					case ClassFileConstants.LongTag :
						dis.skip(8);
						break;
					case ClassFileConstants.DoubleTag :
						dis.skip(8);
						break;
					case ClassFileConstants.ClassTag :
						constantPoolData[i] = dis.readShort();
						break;
					case ClassFileConstants.StringTag :
						dis.skip(2);
						break;
					case ClassFileConstants.FieldRefTag :
						dis.skip(4);
						break;
					case ClassFileConstants.MethodRefTag :
						dis.skip(4);
						break;
					case ClassFileConstants.InterfaceMethodRefTag :
						dis.skip(4);
						break;
					case ClassFileConstants.NameAndTypeTag :
						dis.skip(4);
						break;
					case ClassFileConstants.MethodHandleTag :
						dis.skip(3);
						break;
					case ClassFileConstants.MethodTypeTag :
						dis.skip(2);
						break;
					case ClassFileConstants.InvokeDynamicTag :
						dis.skip(4);
						break;
				}
			}
			
			dis.skip(2);

			// classname
			short classNameIndex = dis.readShort();
			short classNameUTF8index = (Short) constantPoolData[classNameIndex];
			char[] className = ((String) constantPoolData[classNameUTF8index]).toCharArray();

			// superclass name
			short superclassNameIndex = dis.readShort();
			char[] superclassName = null;
			if (superclassNameIndex != 0) {
				short superclassNameUTF8index = (Short) constantPoolData[superclassNameIndex];
				superclassName = ((String) constantPoolData[superclassNameUTF8index]).toCharArray();
			}

			// interfaces
			short interfacesCount = dis.readShort();
			char[][] interfaceNames = null;
			if (interfacesCount != 0) {
				interfaceNames = new char[interfacesCount][];
				for (int i = 0; i < interfacesCount; i++) {
					short interfaceNameIndex = dis.readShort();
					short interfaceNameUTF8index = (Short) constantPoolData[interfaceNameIndex];
					interfaceNames[i] = ((String) constantPoolData[interfaceNameUTF8index]).toCharArray();
				}
			}
			
			return new TypeHierarchyElement(className, superclassName, interfaceNames);
		} catch(Exception e) {
			e.printStackTrace();
		}
		
		return null;
	}

}
