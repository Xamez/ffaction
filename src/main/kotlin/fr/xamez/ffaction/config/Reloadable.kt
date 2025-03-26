package fr.xamez.ffaction.config

@FunctionalInterface
interface Reloadable {

    fun reload(): Boolean

}