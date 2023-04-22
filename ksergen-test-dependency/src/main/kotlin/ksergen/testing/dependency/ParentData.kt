package ksergen.testing.dependency

import kotlinx.serialization.Serializable

@Serializable
class SerializableParentData

@Serializable
open class MutableSerializableParentData

class ParentData

class MutableParentData