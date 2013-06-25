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
import org.springframework.ide.eclipse.core.SpringCore;

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
		String fullyQualifiedClassFileName = new String(fullyQualifiedClassName) + ".class";
		
		String packageName = null;
		String className = null;

		int lastIndexOf = fullyQualifiedClassFileName.lastIndexOf('/');
		if (lastIndexOf > -1) {
			packageName = fullyQualifiedClassFileName.substring(0, lastIndexOf);
			className = fullyQualifiedClassFileName.substring(lastIndexOf + 1);
		}
		else {
			packageName = "";
			className = fullyQualifiedClassFileName;
		}

		for (int i = 0; i < paths.length; i++) {
			InputStream stream = null;
			synchronized(paths[i]) {
				try {
					stream = paths[i].getStream(fullyQualifiedClassFileName, packageName, className);
					if (stream != null) {
						return readTypeHierarchy(stream);
					}
				} catch (Exception e) {
				} finally {
					if (stream != null) {
						try {
							stream.close();
						} catch (IOException e) {
							SpringCore.log(e);
						}
					}
				}
			}
		}
		return null;
	}

	public void cleanup() {
		for (int i = 0; i < paths.length; i++) {
			synchronized(paths[i]) {
				paths[i].cleanup();
			}
		}
	}

	public TypeHierarchyElement readTypeHierarchy(InputStream stream) {
		try {
			DataInputStream dis = new DataInputStream(new BufferedInputStream(stream));
			int magic = dis.readInt(); // magic 0xCAFEBABE
			if (magic != 0xCAFEBABE) {
				throw new IllegalStateException("not bytecode, magic was 0x" + Integer.toString(magic, 16));
			}
			skip(dis, 4);
			
			int constantPoolCount = dis.readShort();
			Object[] constantPoolData = new Object[constantPoolCount];
			for (int i = 1; i < constantPoolCount; i++) {
				int tag = dis.readByte();
				switch (tag) {
					case ClassFileConstants.Utf8Tag :
						constantPoolData[i] = dis.readUTF();
						break;
					case ClassFileConstants.IntegerTag :
						skip(dis, 4);
						break;
					case ClassFileConstants.FloatTag :
						skip(dis, 4);
						break;
					case ClassFileConstants.LongTag :
						skip(dis, 8);
						i++;
						break;
					case ClassFileConstants.DoubleTag :
						skip(dis, 8);
						i++;
						break;
					case ClassFileConstants.ClassTag :
						constantPoolData[i] = dis.readShort();
						break;
					case ClassFileConstants.StringTag :
						skip(dis, 2);
						break;
					case ClassFileConstants.FieldRefTag :
						skip(dis, 4);
						break;
					case ClassFileConstants.MethodRefTag :
						skip(dis, 4);
						break;
					case ClassFileConstants.InterfaceMethodRefTag :
						skip(dis, 4);
						break;
					case ClassFileConstants.NameAndTypeTag :
						skip(dis, 4);
						break;
					case 15 : // ClassFileConstants.MethodHandleTag
						skip(dis, 3);
						break;
					case 16 : // ClassFileConstants.MethodTypeTag
						skip(dis, 2);
						break;
					case 18 : // ClassFileConstants.InvokeDynamicTag
						skip(dis, 4);
						break;
				}
			}
			
			skip(dis, 2);

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
			SpringCore.log(e);
		}
		
		return null;
	}
	
	private void skip(InputStream stream, long n) throws IOException {
		long bytesToSkip = n;
		do {
			long skipped = stream.skip(bytesToSkip);
			bytesToSkip = bytesToSkip - skipped;
		} while (bytesToSkip > 0);
	}

}
