package mb.pie.api

import mb.pie.api.fs.FileSystemResource
import mb.pie.api.stamp.ResourceStamper
import java.io.Serializable

/**
 * Type for task inputs. It must adhere to the following properties:
 *
 * * Implements [Serializable].
 * * Implements [equals][Object.equals] and [hashCode][Object.hashCode].
 * * Must NOT be `null`.
 * * If they input is used as a [key][Key], it must also adhere to [key][Key]'s properties.
 *
 * Failure to adhere to these properties will cause unsound incrementality.
 */
typealias In = Serializable

/**
 * Type for task and resource keys. It must adhere to the following properties:
 *
 * * Implements [Serializable].
 * * Implements [equals][Object.equals] and [hashCode][Object.hashCode].
 * * Must NOT be `null`.
 * * [Equals][Object.equals] and [hashCode][Object.hashCode] must return the same values after a serialization roundtrip (e.g., serialize-deserialize).
 * * The key's serialized bytes must be equal when the key's [equals][Object.equals] method returns true.
 *
 * Failure to adhere to these properties will cause unsound incrementality.
 */
typealias Key = Serializable

/**
 * Type for task outputs. It must adhere to the following properties:
 *
 * * Implements [Serializable].
 * * Implements [equals][Object.equals] and [hashCode][Object.hashCode].
 *
 * Failure to adhere to these properties will cause unsound incrementality.
 */
typealias Out = Serializable?

typealias FileSystemStamper = ResourceStamper<FileSystemResource>