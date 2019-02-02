package mb.pie.api

import java.io.Serializable


enum class Observability : Serializable {
    Attached,
    Observed,
    Detached;
    fun isObservable(): Boolean = this == Observed || this == Attached
    fun isNotObservable() : Boolean = !this.isObservable()
}

fun addOutput(store: StoreWriteTxn, key : TaskKey){
    store.setObservability(key,Observability.Observed)
    for (reqs in store.taskRequires(key)) {
        propegateAttachment(store,reqs.callee)
    }
}

fun propegateAttachment(store: StoreWriteTxn,key: TaskKey) {
    val state = store.observability(key)
    if (state == Observability.Attached) {
        return
    }
    store.setObservability(key, Observability.Attached)
    for (reqs in store.taskRequires(key)) {
        propegateAttachment(store,reqs.callee)
    }

}

fun dropOutput(store: StoreWriteTxn, key : TaskKey){
    val isObserved = store.callersOf(key).map { k -> store.observability(k) }.any{ it.isObservable()}
    if (isObserved ) {
        store.setObservability(key,Observability.Attached)
    } else {
        store.setObservability(key,Observability.Detached)
        for (reqs in store.taskRequires(key)) {
            propegateDetachment(store,reqs.callee)
        }
    }
}

fun propegateDetachment(store: StoreWriteTxn,key: TaskKey) {
    if (store.observability(key).isNotObservable()) {
        return
    }
    val has_attached_parent = store.callersOf(key).map { k -> store.observability(k) }.any{ it.isObservable()}
    if (has_attached_parent) {
        return
    }
    store.setObservability(key, Observability.Detached)
    for (reqs in store.taskRequires(key)) {
        propegateDetachment(store, reqs.callee)
    }
}
