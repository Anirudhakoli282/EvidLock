export interface Azienda{
    piva: string;
    email: string;
    telefono: string;
    ragioneSociale: string;
    indirizzo: string;
    img: string;
}

export interface AuthToken{
    token: string;
    role: string;
}