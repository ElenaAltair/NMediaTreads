package ru.netology.nmedia.util

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer

// класс SingleLiveEvent для события, которое должно обработаться только один раз
class SingleLiveEvent<T> : MutableLiveData<T>() { // делаем SingleLiveEvent наследником MutableLiveData
    // FIXME: упрощённый вариант, пока не прошли Atomic'и
    private var pending = false
    
    override fun observe(owner: LifecycleOwner, observer: Observer<in T?>) {
        require (!hasActiveObservers()) {
            error("Multiple observers registered but only one will be notified of changes.")
        }
        
        super.observe(owner) {
            if (pending) { //если pending == true
                pending = false // то сбрасываем флаг на false
                observer.onChanged(it) // и оповещаем наблюдателя, что значение поменялось
            }
        }
    }

    override fun setValue(t: T?) {
        // если какое-то значение записывается во флаг pending записывается значение true
        pending = true
        super.setValue(t)
    }
}

