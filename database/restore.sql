--
-- NOTE:
--
-- File paths need to be edited. Search for $$PATH$$ and
-- replace it with the path to the directory containing
-- the extracted data files.
--
--
-- PostgreSQL database dump
--

-- Dumped from database version 16.1
-- Dumped by pg_dump version 16.1

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

DROP DATABASE docufy;
--
-- Name: docufy; Type: DATABASE; Schema: -; Owner: postgres
--

CREATE DATABASE docufy WITH TEMPLATE = template0 ENCODING = 'UTF8' LOCALE_PROVIDER = libc LOCALE = 'Italian_Italy.1252';


ALTER DATABASE docufy OWNER TO postgres;

\connect docufy

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

--
-- Name: public; Type: SCHEMA; Schema: -; Owner: pg_database_owner
--

CREATE SCHEMA public;


ALTER SCHEMA public OWNER TO pg_database_owner;

--
-- Name: SCHEMA public; Type: COMMENT; Schema: -; Owner: pg_database_owner
--

COMMENT ON SCHEMA public IS 'standard public schema';


--
-- Name: aumenta_posti_disponibili(); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION public.aumenta_posti_disponibili() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
BEGIN
  -- Aumenta il numero di posti disponibili quando un dipendente viene rimosso da un corso
  UPDATE public.corsi
  SET postidisponibili = postidisponibili + 1
  WHERE id = OLD.corso;

  RETURN OLD;
END;
$$;


ALTER FUNCTION public.aumenta_posti_disponibili() OWNER TO postgres;

--
-- Name: check_stesso_nome_documenti(); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION public.check_stesso_nome_documenti() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
BEGIN
    -- Controlla se esiste un documento con lo stesso nome e dipendente
    IF EXISTS (
        SELECT 1
        FROM documenti
        WHERE nome = NEW.nome AND dipendente = NEW.dipendente
    ) THEN
        -- Se esiste, esegui un aggiornamento al posto di un'insert
        UPDATE documenti
        SET
            nome = NEW.nome,
            url = NEW.url,
            rilascio = NEW.rilascio,
            scadenza = NEW.scadenza,
            stato = NEW.stato,
            formato = NEW.formato
        WHERE
            nome = NEW.nome AND dipendente = NEW.dipendente;
        
        -- Annulla l'inserimento per evitare una nuova riga
        RETURN NULL;
    END IF;

    -- Controlla se esiste un documento con lo stesso nome e azienda
    IF EXISTS (
        SELECT 1
        FROM documenti
        WHERE nome = NEW.nome AND azienda = NEW.azienda
    ) THEN
        -- Se esiste, esegui un aggiornamento al posto di un'insert
        UPDATE documenti
        SET
            nome = NEW.nome,
            url = NEW.url,
            rilascio = NEW.rilascio,
            scadenza = NEW.scadenza,
            stato = NEW.stato,
            formato = NEW.formato
        WHERE
            nome = NEW.nome AND azienda = NEW.azienda;
        
        -- Annulla l'inserimento per evitare una nuova riga
        RETURN NULL;
    END IF;

    -- Se non esiste un documento con lo stesso nome e dipendente o azienda, procedi con l'inserimento
    RETURN NEW;
END;
$$;


ALTER FUNCTION public.check_stesso_nome_documenti() OWNER TO postgres;

--
-- Name: check_valid_azienda_dipendente_in_documenti(); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION public.check_valid_azienda_dipendente_in_documenti() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
BEGIN
    IF NEW.azienda IS NOT NULL AND NEW.dipendente IS NOT NULL THEN
        RAISE EXCEPTION 'Un documento può essere associato solo ad un''azienda o a un dipendente, non entrambi contemporaneamente.';
    ELSIF NEW.azienda IS NULL AND NEW.dipendente IS NULL THEN
        RAISE EXCEPTION 'Un documento deve essere associato ad un''azienda o a un dipendente.';
    ELSIF NEW.azienda IS NOT NULL THEN
        IF NOT EXISTS (SELECT 1 FROM public.aziende WHERE piva = NEW.azienda) THEN
            RAISE EXCEPTION 'Azienda non valida.';
        END IF;
    ELSIF NEW.dipendente IS NOT NULL THEN
        IF NOT EXISTS (SELECT 1 FROM public.dipendenti WHERE id = NEW.dipendente) THEN
            RAISE EXCEPTION 'Dipendente non valido.';
        END IF;
    END IF;
    RETURN NEW;
END;
$$;


ALTER FUNCTION public.check_valid_azienda_dipendente_in_documenti() OWNER TO postgres;

--
-- Name: decrementa_posti_disponibili(); Type: FUNCTION; Schema: public; Owner: postgres
--

CREATE FUNCTION public.decrementa_posti_disponibili() RETURNS trigger
    LANGUAGE plpgsql
    AS $$
BEGIN
  -- Verifica se ci sono posti disponibili prima di consentire l'inserimento
  IF (SELECT postidisponibili FROM public.corsi WHERE id = NEW.corso) > 0 THEN
    -- Decrementa il numero di posti disponibili solo se sono maggiori di zero
    UPDATE public.corsi
    SET postidisponibili = postidisponibili - 1
    WHERE id = NEW.corso;

    RETURN NEW;
  ELSE
    -- Se i posti disponibili sono già a zero, annulla l'insert nella tabella corsi_dipendenti
    RETURN NULL;
  END IF;
END;
$$;


ALTER FUNCTION public.decrementa_posti_disponibili() OWNER TO postgres;

SET default_tablespace = '';

SET default_table_access_method = heap;

--
-- Name: aziende; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.aziende (
    piva character(11) NOT NULL,
    ragionesociale character varying NOT NULL,
    email character varying(319) NOT NULL,
    telefono character(14) NOT NULL,
    indirizzo character varying NOT NULL,
    immagine character varying,
    consulente character(11) NOT NULL
);


ALTER TABLE public.aziende OWNER TO postgres;

--
-- Name: consulenti; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.consulenti (
    piva character(11) NOT NULL,
    ragionesociale character varying NOT NULL,
    email character varying(319) NOT NULL,
    telefono character(14) NOT NULL,
    indirizzo character varying NOT NULL,
    immagine character varying
);


ALTER TABLE public.consulenti OWNER TO postgres;

--
-- Name: corsi; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.corsi (
    id integer NOT NULL,
    nome character varying NOT NULL,
    prezzo double precision NOT NULL,
    descrizione character varying NOT NULL,
    durata integer NOT NULL,
    consulente character(11) NOT NULL,
    categoria character varying NOT NULL,
    posti integer NOT NULL,
    postidisponibili integer NOT NULL,
    esamefinale boolean NOT NULL,
    azienda character(11) NOT NULL
);


ALTER TABLE public.corsi OWNER TO postgres;

--
-- Name: corsi_dipendenti; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.corsi_dipendenti (
    corso integer NOT NULL,
    dipendente integer NOT NULL
);


ALTER TABLE public.corsi_dipendenti OWNER TO postgres;

--
-- Name: corsi_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.corsi_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.corsi_id_seq OWNER TO postgres;

--
-- Name: corsi_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.corsi_id_seq OWNED BY public.corsi.id;


--
-- Name: dipendenti; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.dipendenti (
    id integer NOT NULL,
    cf character(16) NOT NULL,
    nome character varying NOT NULL,
    cognome character varying NOT NULL,
    data_nascita date NOT NULL,
    email character varying(319) NOT NULL,
    telefono character varying(14),
    indirizzo character varying NOT NULL,
    azienda character(11) NOT NULL,
    data_assunzione date NOT NULL,
    ruolo character varying NOT NULL,
    foto character varying
);


ALTER TABLE public.dipendenti OWNER TO postgres;

--
-- Name: dipendenti_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.dipendenti_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.dipendenti_id_seq OWNER TO postgres;

--
-- Name: dipendenti_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.dipendenti_id_seq OWNED BY public.dipendenti.id;


--
-- Name: documenti; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.documenti (
    id integer NOT NULL,
    nome character varying NOT NULL,
    url character varying NOT NULL,
    rilascio date NOT NULL,
    scadenza date NOT NULL,
    dipendente integer,
    azienda character(11),
    stato character varying NOT NULL,
    formato character varying NOT NULL
);


ALTER TABLE public.documenti OWNER TO postgres;

--
-- Name: documenti_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.documenti_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.documenti_id_seq OWNER TO postgres;

--
-- Name: documenti_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.documenti_id_seq OWNED BY public.documenti.id;


--
-- Name: users; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.users (
    piva character(11) NOT NULL,
    email character varying(319) NOT NULL,
    password character varying NOT NULL
);


ALTER TABLE public.users OWNER TO postgres;

--
-- Name: corsi id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.corsi ALTER COLUMN id SET DEFAULT nextval('public.corsi_id_seq'::regclass);


--
-- Name: dipendenti id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.dipendenti ALTER COLUMN id SET DEFAULT nextval('public.dipendenti_id_seq'::regclass);


--
-- Name: documenti id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.documenti ALTER COLUMN id SET DEFAULT nextval('public.documenti_id_seq'::regclass);


--
-- Data for Name: aziende; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.aziende (piva, ragionesociale, email, telefono, indirizzo, immagine, consulente) FROM stdin;
\.
COPY public.aziende (piva, ragionesociale, email, telefono, indirizzo, immagine, consulente) FROM '$$PATH$$/4854.dat';

--
-- Data for Name: consulenti; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.consulenti (piva, ragionesociale, email, telefono, indirizzo, immagine) FROM stdin;
\.
COPY public.consulenti (piva, ragionesociale, email, telefono, indirizzo, immagine) FROM '$$PATH$$/4855.dat';

--
-- Data for Name: corsi; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.corsi (id, nome, prezzo, descrizione, durata, consulente, categoria, posti, postidisponibili, esamefinale, azienda) FROM stdin;
\.
COPY public.corsi (id, nome, prezzo, descrizione, durata, consulente, categoria, posti, postidisponibili, esamefinale, azienda) FROM '$$PATH$$/4856.dat';

--
-- Data for Name: corsi_dipendenti; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.corsi_dipendenti (corso, dipendente) FROM stdin;
\.
COPY public.corsi_dipendenti (corso, dipendente) FROM '$$PATH$$/4857.dat';

--
-- Data for Name: dipendenti; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.dipendenti (id, cf, nome, cognome, data_nascita, email, telefono, indirizzo, azienda, data_assunzione, ruolo, foto) FROM stdin;
\.
COPY public.dipendenti (id, cf, nome, cognome, data_nascita, email, telefono, indirizzo, azienda, data_assunzione, ruolo, foto) FROM '$$PATH$$/4859.dat';

--
-- Data for Name: documenti; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.documenti (id, nome, url, rilascio, scadenza, dipendente, azienda, stato, formato) FROM stdin;
\.
COPY public.documenti (id, nome, url, rilascio, scadenza, dipendente, azienda, stato, formato) FROM '$$PATH$$/4861.dat';

--
-- Data for Name: users; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.users (piva, email, password) FROM stdin;
\.
COPY public.users (piva, email, password) FROM '$$PATH$$/4863.dat';

--
-- Name: corsi_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.corsi_id_seq', 10, true);


--
-- Name: dipendenti_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.dipendenti_id_seq', 80, true);


--
-- Name: documenti_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.documenti_id_seq', 32, true);


--
-- Name: aziende azienda_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.aziende
    ADD CONSTRAINT azienda_pkey PRIMARY KEY (piva);


--
-- Name: aziende azienda_telefono_key; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.aziende
    ADD CONSTRAINT azienda_telefono_key UNIQUE (telefono);


--
-- Name: aziende aziende_email_key; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.aziende
    ADD CONSTRAINT aziende_email_key UNIQUE (email);


--
-- Name: consulenti consulenti_email_key; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.consulenti
    ADD CONSTRAINT consulenti_email_key UNIQUE (email);


--
-- Name: consulenti consulenti_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.consulenti
    ADD CONSTRAINT consulenti_pkey PRIMARY KEY (piva);


--
-- Name: consulenti consulenti_telefono_key; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.consulenti
    ADD CONSTRAINT consulenti_telefono_key UNIQUE (telefono);


--
-- Name: corsi_dipendenti corsi_dipendenti_pk; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.corsi_dipendenti
    ADD CONSTRAINT corsi_dipendenti_pk PRIMARY KEY (corso, dipendente);


--
-- Name: corsi corsi_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.corsi
    ADD CONSTRAINT corsi_pkey PRIMARY KEY (id);


--
-- Name: dipendenti dipendenti_cf_key; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.dipendenti
    ADD CONSTRAINT dipendenti_cf_key UNIQUE (cf);


--
-- Name: dipendenti dipendenti_email_key; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.dipendenti
    ADD CONSTRAINT dipendenti_email_key UNIQUE (email);


--
-- Name: dipendenti dipendenti_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.dipendenti
    ADD CONSTRAINT dipendenti_pkey PRIMARY KEY (id);


--
-- Name: dipendenti dipendenti_telefono_key; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.dipendenti
    ADD CONSTRAINT dipendenti_telefono_key UNIQUE (telefono);


--
-- Name: documenti documenti_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.documenti
    ADD CONSTRAINT documenti_pkey PRIMARY KEY (id);


--
-- Name: users users_email_key; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.users
    ADD CONSTRAINT users_email_key UNIQUE (email);


--
-- Name: users users_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.users
    ADD CONSTRAINT users_pkey PRIMARY KEY (piva);


--
-- Name: corsi_dipendenti aumenta_posti_trigger; Type: TRIGGER; Schema: public; Owner: postgres
--

CREATE TRIGGER aumenta_posti_trigger AFTER DELETE ON public.corsi_dipendenti FOR EACH ROW EXECUTE FUNCTION public.aumenta_posti_disponibili();


--
-- Name: documenti before_insert_document; Type: TRIGGER; Schema: public; Owner: postgres
--

CREATE TRIGGER before_insert_document BEFORE INSERT ON public.documenti FOR EACH ROW EXECUTE FUNCTION public.check_valid_azienda_dipendente_in_documenti();


--
-- Name: corsi_dipendenti decrementa_posti_trigger; Type: TRIGGER; Schema: public; Owner: postgres
--

CREATE TRIGGER decrementa_posti_trigger BEFORE INSERT ON public.corsi_dipendenti FOR EACH ROW EXECUTE FUNCTION public.decrementa_posti_disponibili();


--
-- Name: documenti update_stesso_nome_documenti; Type: TRIGGER; Schema: public; Owner: postgres
--

CREATE TRIGGER update_stesso_nome_documenti BEFORE INSERT ON public.documenti FOR EACH ROW EXECUTE FUNCTION public.check_stesso_nome_documenti();

ALTER TABLE public.documenti DISABLE TRIGGER update_stesso_nome_documenti;


--
-- Name: aziende aziende_consulente_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.aziende
    ADD CONSTRAINT aziende_consulente_fkey FOREIGN KEY (consulente) REFERENCES public.consulenti(piva) ON DELETE CASCADE;


--
-- Name: aziende aziende_users_email_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.aziende
    ADD CONSTRAINT aziende_users_email_fk FOREIGN KEY (email) REFERENCES public.users(email) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: aziende aziende_users_piva_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.aziende
    ADD CONSTRAINT aziende_users_piva_fk FOREIGN KEY (piva) REFERENCES public.users(piva) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: consulenti consulenti_users_email_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.consulenti
    ADD CONSTRAINT consulenti_users_email_fk FOREIGN KEY (email) REFERENCES public.users(email) ON UPDATE CASCADE ON DELETE CASCADE;


--
-- Name: consulenti consulenti_users_piva_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.consulenti
    ADD CONSTRAINT consulenti_users_piva_fk FOREIGN KEY (piva) REFERENCES public.users(piva) ON DELETE CASCADE;


--
-- Name: corsi corsi_azienda_fk; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.corsi
    ADD CONSTRAINT corsi_azienda_fk FOREIGN KEY (azienda) REFERENCES public.aziende(piva);


--
-- Name: corsi corsi_consulente_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.corsi
    ADD CONSTRAINT corsi_consulente_fkey FOREIGN KEY (consulente) REFERENCES public.consulenti(piva) ON DELETE CASCADE;


--
-- Name: corsi_dipendenti corsi_dipendenti_corso_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.corsi_dipendenti
    ADD CONSTRAINT corsi_dipendenti_corso_fkey FOREIGN KEY (corso) REFERENCES public.corsi(id) ON DELETE CASCADE;


--
-- Name: corsi_dipendenti corsi_dipendenti_dipendente_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.corsi_dipendenti
    ADD CONSTRAINT corsi_dipendenti_dipendente_fkey FOREIGN KEY (dipendente) REFERENCES public.dipendenti(id) ON DELETE CASCADE;


--
-- Name: dipendenti dipendenti_azienda_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.dipendenti
    ADD CONSTRAINT dipendenti_azienda_fkey FOREIGN KEY (azienda) REFERENCES public.aziende(piva) ON DELETE CASCADE;


--
-- PostgreSQL database dump complete
--

