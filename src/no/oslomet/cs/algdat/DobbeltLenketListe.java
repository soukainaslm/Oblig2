package no.oslomet.cs.algdat;


////////////////// class DobbeltLenketListe //////////////////////////////


import sun.reflect.generics.reflectiveObjects.NotImplementedException;

import java.util.Comparator;
import java.util.ConcurrentModificationException;
import java.util.NoSuchElementException;
import java.util.StringJoiner;

import java.util.Iterator;
import java.util.Objects;
import java.util.function.Predicate;



public class DobbeltLenketListe<T> implements Liste<T> {

    /**
     * Node class
     * @param <T>
     */
    private static final class Node<T> {
        private T verdi;                   // nodens verdi
        private Node<T> forrige, neste;    // pekere

        private Node(T verdi, Node<T> forrige, Node<T> neste) {
            this.verdi = verdi;
            this.forrige = forrige;
            this.neste = neste;
        }

        private Node(T verdi) {
            this(verdi, null, null);
        }
    }

    // instansvariabler
    private Node<T> hode;          // peker til den første i listen
    private Node<T> hale;          // peker til den siste i listen
    private int antall;            // antall noder i listen
    private int endringer;         // antall endringer i listen

    private Node<T> finnNode(int indeks){
        Node<T> p;

        if(indeks < antall / 2){
            p = hode;
            for(int i = 0; i < indeks; i++){
                p = p.neste;
            }
            return p;
        } else {
            p = hale;
            for(int i = antall - 1; i > indeks; i--){
                p = p.forrige;
            }
            return p;
        }
    }

    private static void fratilKontroll(int antall, int fra, int til)
    {
        if (fra < 0) throw new IndexOutOfBoundsException("fra: " + fra + ", er negativ!");

        if (til > antall) throw new IndexOutOfBoundsException("til: " + til + " er større enn antall: " + antall);

        if (fra > til) throw new IllegalArgumentException("fra(" + fra + ") er større enn til(" + til + ") - ulovlig intervall!");
    }



    public DobbeltLenketListe() {

        hode = hale = null;
        antall = 0;
        endringer = 0;;
    }

    public DobbeltLenketListe(T[] a) {
        this();

        Objects.requireNonNull(a, "Tabellen a er null!");

        hode = hale = new Node<>(null);

        for (T verdi : a)
        {
            if (verdi != null)
            {
                hale = hale.neste = new Node<>(verdi, hale, null);
                antall++;
            }
        }

        if (antall == 0) hode = hale = null;
        else (hode = hode.neste).forrige = null;
    }

    public Liste<T> subliste(int fra, int til){

        fratilKontroll(antall, fra, til);

        DobbeltLenketListe<T> subListe = new DobbeltLenketListe<>();

        Node<T> p = finnNode(fra);

        for (int i = fra; i < til; i++) {
            subListe.leggInn(p.verdi);
            p = p.neste;
        }
        return subListe;
    }

    @Override
    public int antall()
    {
        return antall;
    }

    @Override
    public boolean tom() {
        return antall == 0;
    }

    @Override
    public boolean leggInn(T verdi) {
        Objects.requireNonNull(verdi, "Null-verdierikke tillatt!");

        Node<T> p = new Node<>(verdi, hale, null);
        hale = tom() ? (hode = p) : (hale.neste = p);

        antall++;
        endringer++;


        return true;
    }

    @Override
    public void leggInn(int indeks, T verdi) {

        Objects.requireNonNull(verdi, "Ikke tillatt med null-verdier!");

        indeksKontroll(indeks, true);

        if (tom())
        {
            hode = hale = new Node<>(verdi, null, null);
        }
        else if (indeks == 0)
        {
            hode = hode.forrige = new Node<>(verdi, null, hode);
        }
        else if (indeks == antall)
        {
            hale = hale.neste = new Node<>(verdi, hale, null);
        }
        else
        {
            Node<T> p = finnNode(indeks);
            p.forrige = p.forrige.neste = new Node<>(verdi, p.forrige, p);
        }

        antall++;
        endringer++;

    }

    @Override
    public boolean inneholder(T verdi) {

        if (indeksTil(verdi) > -1) return true;
        else if (indeksTil(verdi) < 0)
            return false;
        throw new NotImplementedException();
    }

    @Override
    public T hent(int indeks) {
        indeksKontroll(indeks, false);
        Node<T> p = finnNode(indeks);

        return p.verdi;
    }

    @Override
    public int indeksTil(T verdi) {
        int index = 0;
        if (!verdi.equals(null)) return -1;
        for (Node<T> temp = hale; temp.forrige != null; temp = temp.forrige) {
            if (temp.verdi.equals(verdi)) {
                return index;
            }
            else if (temp.equals(hode) && !temp.verdi.equals(verdi)) return -1;
            index++;

        }
        throw new NotImplementedException();
    }

    @Override
    public T oppdater(int indeks, T nyverdi) {
        Objects.requireNonNull(nyverdi, "Nyverdi kan ikke være null!");
        indeksKontroll(indeks, false);

        Node<T> p = finnNode(indeks);

        T gammelverdi = p.verdi;
        p.verdi = nyverdi;

        endringer++;

        return gammelverdi;
    }
    private T fjernNode(Node<T> p)  // lager en hjelpemetode
    {
        if (p == hode)
        {
            if (antall == 1) hode = hale = null;
            else (hode = hode.neste).forrige = null;
        }
        else if (p == hale) (hale = hale.forrige).neste = null;
        else (p.forrige.neste = p.neste).forrige = p.forrige;

        antall--;
        endringer++;

        return p.verdi;
    }

    @Override
    public boolean fjern(T verdi) {
        if (verdi == null) return false;

        for (Node<T> p = hode; p != null; p = p.neste)
        {
            if (p.verdi.equals(verdi))
            {
                fjernNode(p);   // bruker hjelpemetoden
                return true;
            }
        }
        return false;
    }

    @Override
    public T fjern(int indeks) {

        indeksKontroll(indeks, false);
        return fjernNode(finnNode(indeks));
    }

    @Override
    public void nullstill() {
        /*
        //1.måten
        for (Node<T> temp = hode; temp.neste != null; temp = temp.neste){
            temp.verdi = null;
            temp.forrige = null;
            temp.neste = null;
            endringer++;
        }
        hode = hale = null;
        antall = 0;
*/
       //2.måten
        int index = 0;
        for (Node<T> temp = hode; temp.neste != null; temp = temp.neste) {
            fjern(index);
            index ++;
        }
        throw new NotImplementedException();

    }

    @Override
    public String toString() {

        if (tom()) return "[]";

        StringBuilder sb = new StringBuilder();
        sb.append('[');

        Node<T> node = hode;

        sb.append(node.verdi);
        node = node.neste;

        while (node != null) {
            sb.append(',').append(' ').append(node.verdi);
            node = node.neste;
        }

        sb.append(']');
        return sb.toString();
    }

    public String omvendtString() {
        if (tom()) return "[]";

        StringBuilder sb = new StringBuilder();
        sb.append('[');

        Node<T> node = hale;

        sb.append(node.verdi);
        node = node.forrige;

        while (node != null) {
            sb.append(',').append(' ').append(node.verdi);
            node = node.forrige; //Verdiene kommer i omvendt rekkefølge
        }

        sb.append(']');
        return sb.toString();
    }

    @Override
    public Iterator<T> iterator() {
        return new DobbeltLenketListeIterator();
    }

    public Iterator<T> iterator(int indeks) {

        indeksKontroll(indeks, false);

        return new DobbeltLenketListeIterator(indeks);

    }

    private class DobbeltLenketListeIterator implements Iterator<T>
    {
        private Node<T> denne;
        private boolean fjernOK;
        private int iteratorendringer;

        private DobbeltLenketListeIterator(){

            denne = hode;     // denne starter på den første i listen
            fjernOK = false;  // blir sann når next() kalles
            iteratorendringer = endringer;  // teller endringer
        }

        private DobbeltLenketListeIterator(int indeks){
            denne = finnNode(indeks);
            fjernOK = false;
            iteratorendringer = endringer;


        }

        @Override
        public boolean hasNext(){
            return denne != null;
        }

        @Override
        public T next(){

            if(!hasNext())
                throw new NoSuchElementException();

            if(iteratorendringer != endringer)
                throw new ConcurrentModificationException();

            T temp = denne.verdi;
            denne = denne.neste;

            fjernOK = true;

            return temp;
        }

        @Override
        public void remove(){

            if(!fjernOK) throw new IllegalStateException("Ulovlig å fjerne en verdi");

            if(iteratorendringer != endringer) throw new ConcurrentModificationException("Listen er endret");

            fjernOK = false;

            if(antall == 1) hode = hale = null;
            else if(denne == null){
                hale = hale.forrige;
                hale.neste = null;
            }
            else if(denne.forrige == hode){
                hode = hode.neste;
                hode.forrige = null;
            } else {
                Node<T> node = denne.forrige;
                node.forrige.neste = denne;
                node.neste.forrige = node.forrige;
                node = null;
            }

            iteratorendringer++;
            endringer++;
            antall--;
        }

    } // class DobbeltLenketListeIterator

    public static <T> void sorter(Liste<T> liste, Comparator<? super T> c) {

        if (liste.antall() == 0) throw new NotImplementedException();
        for (Node<T> current = ((no.oslomet.cs.algdat.DobbeltLenketListe) liste).hode; current.neste != null; current = current.neste) {
            for (Node<T> other = current.neste; other.neste != null; other = other.neste) {
                if (c.compare(current.verdi, other.verdi) > 0) {
                    T temp = other.verdi;
                    other.verdi = current.verdi;
                    current.verdi = temp;
                }
            }
        }
    }
} // class DobbeltLenketListe


