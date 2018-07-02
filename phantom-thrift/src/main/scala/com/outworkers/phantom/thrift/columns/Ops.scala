/*
 * Copyright 2013 - 2017 Outworkers Ltd.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.outworkers.phantom.thrift.columns

import com.outworkers.phantom.builder.primitives.Primitive
import com.outworkers.phantom.thrift.ThriftHelper
import com.twitter.scrooge.{ThriftStruct, ThriftStructSerializer}

trait Ops[Serializer[X <: ThriftStruct] <: ThriftStructSerializer[X]] {

  type ThriftStruct = com.twitter.scrooge.ThriftStruct

  implicit def thriftPrimitive[
    T <: ThriftStruct
  ](implicit hp: ThriftHelper[T, Serializer[T]]): Primitive[T] = {
    Primitive.derive[T, String](hp.serializer.toString)(hp.serializer.fromString)
  }
}
