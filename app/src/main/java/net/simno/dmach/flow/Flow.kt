package net.simno.dmach.flow

import android.view.View
import android.widget.CompoundButton
import android.widget.TextView
import androidx.annotation.CheckResult
import androidx.core.widget.addTextChangedListener
import androidx.lifecycle.LifecycleCoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.conflate
import kotlinx.coroutines.flow.flowOn

fun <T> Flow<T>.launchWhenCreatedIn(scope: LifecycleCoroutineScope) = scope.launchWhenCreated {
    collect()
}

@CheckResult
fun View.clicks(): Flow<Unit> = callbackFlow {
    val listener = View.OnClickListener {
        runCatching { offer(Unit) }
    }
    setOnClickListener(listener)
    awaitClose { setOnClickListener(null) }
}.conflate().flowOn(Dispatchers.Main)

@CheckResult
fun View.longClicks(): Flow<Unit> = callbackFlow {
    val listener = View.OnLongClickListener {
        runCatching { offer(Unit) }
        true
    }
    setOnLongClickListener(listener)
    awaitClose { setOnClickListener(null) }
}.conflate().flowOn(Dispatchers.Main)

@CheckResult
fun View.selections(): Flow<Pair<Int, Boolean>> = callbackFlow {
    val listener = View.OnClickListener {
        runCatching { offer(tag.toString().toInt() to isSelected) }
    }
    setOnClickListener(listener)
    awaitClose { setOnClickListener(null) }
}.conflate().flowOn(Dispatchers.Main)

@CheckResult
fun CompoundButton.checkedChanges(): Flow<Boolean> = callbackFlow {
    val listener = CompoundButton.OnCheckedChangeListener { _, isChecked ->
        runCatching { offer(isChecked) }
    }
    setOnCheckedChangeListener(listener)
    awaitClose { setOnCheckedChangeListener(null) }
}.conflate().flowOn(Dispatchers.Main)

@CheckResult
fun TextView.textChanges(): Flow<String> = callbackFlow {
    val listener = addTextChangedListener(
        onTextChanged = { text, _, _, _ ->
            runCatching { offer(text.toString()) }
        }
    )
    runCatching { offer(text.toString()) }
    awaitClose { removeTextChangedListener(listener) }
}.conflate().flowOn(Dispatchers.Main)
