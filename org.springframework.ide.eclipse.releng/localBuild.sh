#!/bin/sh
################################################################################
# Copyright (c) 2005, 2008 Spring IDE Developers
# All rights reserved. This program and the accompanying materials
# are made available under the terms of the Eclipse Public License v1.0
# which accompanies this distribution, and is available at
# http://www.eclipse.org/legal/epl-v10.html
# 
# Contributors:
#     Spring IDE Developers - initial API and implementation
################################################################################

./build.sh -Djava.rt=/System/Library/Frameworks/JavaVM.framework/Versions/1.5.0/Classes/classes.jar -Dpack200.enable=true $@
