/*
 * Copyright (c) 2020. Dylan Cai
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

@file:Suppress("unused")

package com.dylanc.viewbinding.nonreflection

import android.app.Activity
import android.app.Dialog
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import androidx.viewbinding.ViewBinding
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty


/**
 * @author Dylan Cai
 */

fun <VB : ViewBinding> Activity.binding(inflate: (LayoutInflater) -> VB) = lazy {
  inflate(layoutInflater).apply { setContentView(root) }
}

fun <VB : ViewBinding> Fragment.binding(bind: (View) -> VB) =
  FragmentBindingDelegate(bind)

fun <VB : ViewBinding> Dialog.binding(inflate: (LayoutInflater) -> VB) = lazy {
  inflate(layoutInflater).apply { setContentView(root) }
}

fun <VB : ViewBinding> ViewGroup.binding(
  inflate: (LayoutInflater, ViewGroup?, Boolean) -> VB,
  attachToParent: Boolean = true
) =
  if (attachToParent)
    inflate(LayoutInflater.from(context), this, true)
  else
    inflate(LayoutInflater.from(context), null, false)

class FragmentBindingDelegate<VB : ViewBinding>(
  private val bind: (View) -> VB
) : ReadOnlyProperty<Fragment, VB> {

  private var isInitialized = false
  private var _binding: VB? = null
  private val binding: VB get() = _binding!!

  @Suppress("UNCHECKED_CAST")
  override fun getValue(thisRef: Fragment, property: KProperty<*>): VB {
    if (!isInitialized) {
      thisRef.viewLifecycleOwner.lifecycle.addObserver(object : LifecycleObserver {
        @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
        fun onDestroyView() {
          _binding = null
        }
      })
      _binding = bind(thisRef.requireView())
      isInitialized = true
    }
    return binding
  }
}