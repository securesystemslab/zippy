/*
 * Copyright 2000-2010 Sun Microsystems, Inc.  All Rights Reserved.
 * DO NOT ALTER OR REMOVE COPYRIGHT NOTICES OR THIS FILE HEADER.
 *
 * This code is free software; you can redistribute it and/or modify it
 * under the terms of the GNU General Public License version 2 only, as
 * published by the Free Software Foundation.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License
 * version 2 for more details (a copy is included in the LICENSE file that
 * accompanied this code).
 *
 * You should have received a copy of the GNU General Public License version
 * 2 along with this work; if not, write to the Free Software Foundation,
 * Inc., 51 Franklin St, Fifth Floor, Boston, MA 02110-1301 USA.
 *
 * Please contact Sun Microsystems, Inc., 4150 Network Circle, Santa Clara,
 * CA 95054 USA or visit www.sun.com if you need additional information or
 * have any questions.
 *
 */

#define CC (char*)  /*cast a literal from (const char*)*/
#define FN_PTR(f) CAST_FROM_FN_PTR(void*, &f)

#ifdef SOLARIS
#define JNIEXPORT
#define JNICALL
#endif

/*
* Class:     com_sun_hotspot_c1x_VMEntries
* Method:    RiMethod_code
* Signature: (Ljava/lang/Object;)[B
*/
JNIEXPORT jbyteArray JNICALL Java_com_sun_hotspot_c1x_VMEntries_RiMethod_1code
  (JNIEnv *, jobject, jobject);

/*
* Class:     com_sun_hotspot_c1x_VMEntries
* Method:    RiMethod_maxStackSize
* Signature: (Ljava/lang/Object;)I
*/
JNIEXPORT jint JNICALL Java_com_sun_hotspot_c1x_VMEntries_RiMethod_1maxStackSize
  (JNIEnv *, jobject, jobject);

/*
* Class:     com_sun_hotspot_c1x_VMEntries
* Method:    RiMethod_maxLocals
* Signature: (Ljava/lang/Object;)I
*/
JNIEXPORT jint JNICALL Java_com_sun_hotspot_c1x_VMEntries_RiMethod_1maxLocals
  (JNIEnv *, jobject, jobject);


/*
* Class:     com_sun_hotspot_c1x_VMEntries
* Method:    RiMethod_holder
* Signature: (Ljava/lang/Object;)Lcom/sun/cri/ri/RiType;
*/
JNIEXPORT jobject JNICALL Java_com_sun_hotspot_c1x_VMEntries_RiMethod_1holder
  (JNIEnv *, jobject, jobject);

/*
* Class:     com_sun_hotspot_c1x_VMEntries
* Method:    RiMethod_signature
* Signature: (Ljava/lang/Object;)Lcom/sun/cri/ri/RiSignature;
*/
JNIEXPORT jstring JNICALL Java_com_sun_hotspot_c1x_VMEntries_RiMethod_1signature
  (JNIEnv *, jobject, jobject);

/*
* Class:     com_sun_hotspot_c1x_VMEntries
* Method:    RiMethod_name
* Signature: (Ljava/lang/Object;)Ljava/lang/String;
*/
JNIEXPORT jstring JNICALL Java_com_sun_hotspot_c1x_VMEntries_RiMethod_1name
  (JNIEnv *, jobject, jobject);

/*
* Class:     com_sun_hotspot_c1x_VMEntries
* Method:    RiSignature_lookupType
* Signature: (Ljava/lang/String;Lcom/sun/cri/ri/RiType;)Lcom/sun/cri/ri/RiType;
*/
JNIEXPORT jobject JNICALL Java_com_sun_hotspot_c1x_VMEntries_RiSignature_1lookupType
    (JNIEnv *, jobject, jstring, jobject);

/*
* Class:     com_sun_hotspot_c1x_VMEntries
* Method:    RiSignature_symbolToString
* Signature: (Ljava/lang/Object;)Ljava/lang/String;
*/
JNIEXPORT jstring JNICALL Java_com_sun_hotspot_c1x_VMEntries_RiSignature_1symbolToString
(JNIEnv *, jobject, jobject);

/*
* Class:     com_sun_hotspot_c1x_VMEntries
* Method:    RiType_javaClass
* Signature: (Ljava/lang/Object;)Ljava/lang/Class;
*/
JNIEXPORT jclass JNICALL Java_com_sun_hotspot_c1x_VMEntries_RiType_1javaClass
(JNIEnv *, jobject, jobject);

/*
* Class:     com_sun_hotspot_c1x_VMEntries
* Method:    RiType_name
* Signature: (Ljava/lang/Object;)Ljava/lang/String;
*/
JNIEXPORT jstring JNICALL Java_com_sun_hotspot_c1x_VMEntries_RiType_1name
(JNIEnv *, jobject, jobject);


/*
* Class:     com_sun_hotspot_c1x_VMEntries
* Method:    RiConstantPool_lookupConstant
* Signature: (Ljava/lang/Object;I)Ljava/lang/Object;
*/
JNIEXPORT jobject JNICALL Java_com_sun_hotspot_c1x_VMEntries_RiConstantPool_1lookupConstant
(JNIEnv *, jobject, jobject, jint);

/*
* Class:     com_sun_hotspot_c1x_VMEntries
* Method:    RiConstantPool_lookupMethod
* Signature: (Ljava/lang/Object;I)Lcom/sun/cri/ri/RiMethod;
*/
JNIEXPORT jobject JNICALL Java_com_sun_hotspot_c1x_VMEntries_RiConstantPool_1lookupMethod
(JNIEnv *, jobject, jobject, jint, jbyte);

/*
* Class:     com_sun_hotspot_c1x_VMEntries
* Method:    RiConstantPool_lookupSignature
* Signature: (Ljava/lang/Object;I)Lcom/sun/cri/ri/RiSignature;
*/
JNIEXPORT jobject JNICALL Java_com_sun_hotspot_c1x_VMEntries_RiConstantPool_1lookupSignature
(JNIEnv *, jobject, jobject, jint);

/*
* Class:     com_sun_hotspot_c1x_VMEntries
* Method:    RiConstantPool_lookupType
* Signature: (Ljava/lang/Object;I)Lcom/sun/cri/ri/RiType;
*/
JNIEXPORT jobject JNICALL Java_com_sun_hotspot_c1x_VMEntries_RiConstantPool_1lookupType
(JNIEnv *, jobject, jobject, jint);

/*
* Class:     com_sun_hotspot_c1x_VMEntries
* Method:    RiConstantPool_lookupField
* Signature: (Ljava/lang/Object;I)Lcom/sun/cri/ri/RiField;
*/
JNIEXPORT jobject JNICALL Java_com_sun_hotspot_c1x_VMEntries_RiConstantPool_1lookupField
(JNIEnv *, jobject, jobject, jint);

/*
* Class:     com_sun_hotspot_c1x_VMEntries
* Method:    findRiType
* Signature: (Ljava/lang/Object;)Lcom/sun/cri/ri/RiType;
*/
JNIEXPORT jobject JNICALL Java_com_sun_hotspot_c1x_VMEntries_findRiType
(JNIEnv *, jobject, jobject);

/*
* Class:     com_sun_hotspot_c1x_VMEntries
* Method:    RiRuntime_getConstantPool
* Signature: (Ljava/lang/Object;)Lcom/sun/cri/ri/RiConstantPool;
*/
JNIEXPORT jobject JNICALL Java_com_sun_hotspot_c1x_VMEntries_RiRuntime_1getConstantPool
(JNIEnv *, jobject, jobject);

/*
* Class:     com_sun_hotspot_c1x_VMEntries
* Method:    RiType_isArrayClass
* Signature: (Ljava/lang/Object;)Z
*/
JNIEXPORT jboolean JNICALL Java_com_sun_hotspot_c1x_VMEntries_RiType_1isArrayClass
(JNIEnv *, jobject, jobject);

/*
* Class:     com_sun_hotspot_c1x_VMEntries
* Method:    RiType_isInstanceClass
* Signature: (Ljava/lang/Object;)Z
*/
JNIEXPORT jboolean JNICALL Java_com_sun_hotspot_c1x_VMEntries_RiType_1isInstanceClass
(JNIEnv *, jobject, jobject);

/*
* Class:     com_sun_hotspot_c1x_VMEntries
* Method:    RiType_isInterface
* Signature: (Ljava/lang/Object;)Z
*/
JNIEXPORT jboolean JNICALL Java_com_sun_hotspot_c1x_VMEntries_RiType_1isInterface
(JNIEnv *, jobject, jobject);

/*
* Class:     com_sun_hotspot_c1x_VMEntries
* Method:    RiMethod_accessFlags
* Signature: (Ljava/lang/Object;)I
*/
JNIEXPORT jint JNICALL Java_com_sun_hotspot_c1x_VMEntries_RiMethod_1accessFlags
(JNIEnv *, jobject, jobject);

/*
* Class:     com_sun_hotspot_c1x_VMEntries
* Method:    installCode
* Signature: (Lcom/sun/hotspot/c1x/HotSpotTargetMethod;)V
*/
JNIEXPORT void JNICALL Java_com_sun_hotspot_c1x_VMEntries_installCode
(JNIEnv *, jobject, jobject);

/*
* Class:     com_sun_hotspot_c1x_VMEntries
* Method:    getConfiguration
* Signature: ()Lcom/sun/hotspot/c1x/HotSpotVMConfig;
*/
JNIEXPORT jobject JNICALL Java_com_sun_hotspot_c1x_VMEntries_getConfiguration
(JNIEnv *, jobject);


extern JNINativeMethod VMEntries_methods[];
int VMEntries_methods_count();
