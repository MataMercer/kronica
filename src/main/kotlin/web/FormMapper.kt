package org.matamercer.web

import io.javalin.http.UploadedFile

inline fun<reified T> formMapper(paramMap: Map<String, List<String>>,  uploadMap: Map<String, List<UploadedFile>>):T{
    val clazz = T::class.java

    val obj = clazz.getDeclaredConstructor().newInstance()
    clazz.declaredFields.forEach { field ->
        val name = field.name
        val value = paramMap[name]?.firstOrNull()

            field.isAccessible = true
            when (field.type) {
                Long::class.java -> {
                    field.set(obj, value?.toLong())
                }
                Integer::class.java -> {
                    field.set(obj, value?.toInt())
                }
                Boolean::class.java -> {
                    field.set(obj, value.toBoolean())
                }
                Double::class.java -> {
                    field.set(obj, value?.toDouble())
                }
                Float::class.java -> {
                    field.set(obj, value?.toFloat())
                }
                Short::class.java -> {
                    field.set(obj, value?.toShort())
                }
                Byte::class.java -> {
                    field.set(obj, value?.toByte())
                }
                Char::class.java -> {
                    field.set(obj, value?.single())
                }
                String::class.java -> {
                    field.set(obj, value)
                }
                List::class.java ->{
                    //hack because I have no idea how to get parameterized types lmao
                    //only for upload file fields.
                    if (field.genericType.typeName.contains(UploadedFile::class.java.typeName) ){
                        val uploadValue = uploadMap[name]
                        if (uploadValue!=null){
                            field.set(obj, uploadValue)
                        }
                    }
                    else{
                        field.set(obj, value)
                    }

                }
        }
    }
    return obj
}