export interface Corso{
    id: number;
    nome: string;
    categoria: string;
    prezzo: number;
    descrizione: string;
    durata: number;
    posti: number;
    postiDisponibili: number;
    esameFinale: boolean;
}