package com.beautifulapp.basis_adk.helper

typealias Func<A, B> = (A) -> B
typealias AsyncFunc0<A, B> = ((A) -> B) -> Unit
typealias AsyncFunc1<A, B> = (Any?, (A) -> B) -> Unit
typealias AsyncFunc2<A, B, Y, Z> = (p0: Y, p1: Z, p2: (A) -> B) -> Unit
typealias AsyncFunc2B<A, B> = (p0: Any?, p1: Any?, p2: (A) -> B) -> Unit


infix fun <B, C> Func<B, C>.o(f: () -> B): () -> C = { this(f()) }
infix fun <A, B, C> Func<B, C>.o(f: Func<A, B>): Func<A, C> = { x: A -> this(f(x)) }

//infix fun <A, B, C, Y, Z> Func<B, C>.o(f: AsyncFunc2<A, B,Y,Z>): Func<A, C> = { x: A -> this(f.javaClass.typeParameters) }










