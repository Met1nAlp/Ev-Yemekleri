package com.example.evyemekleri.Model

data class Yemek(
    val isim: String,
    val malzemeler: String,
    val tarif: String,
    val tur: String
)

data class Mesrubat(
    val isim: String,
    val malzemeler: String,
    val tarif: String,
    val tur: String
)

data class Tatli(
    val isim: String,
    val malzemeler: String,
    val tarif: String,
    val tur: String
)

data class Corba(
    val isim: String,
    val malzemeler: String,
    val tarif: String,
    val tur: String
)

data class Meze(
    val isim: String,
    val malzemeler: String,
    val tarif: String,
    val tur: String
) 