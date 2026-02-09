--
-- PostgreSQL database dump
--

\restrict e9GJRDZW2iNF03bnprs1eMvTxwfzrOLnlALALu1hfbR0kpxqdKv4db2hqeg3cLw

-- Dumped from database version 18.0
-- Dumped by pg_dump version 18.0

-- Started on 2026-02-09 19:45:57

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET transaction_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

SET default_tablespace = '';

SET default_table_access_method = heap;

--
-- TOC entry 226 (class 1259 OID 17099)
-- Name: cennik; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.cennik (
    id integer NOT NULL,
    produkt_id integer NOT NULL,
    cena numeric(10,2) DEFAULT 0.00 NOT NULL
);


ALTER TABLE public.cennik OWNER TO postgres;

--
-- TOC entry 225 (class 1259 OID 17098)
-- Name: cennik_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.cennik_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.cennik_id_seq OWNER TO postgres;

--
-- TOC entry 5055 (class 0 OID 0)
-- Dependencies: 225
-- Name: cennik_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.cennik_id_seq OWNED BY public.cennik.id;


--
-- TOC entry 220 (class 1259 OID 17053)
-- Name: kategorie; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.kategorie (
    id integer NOT NULL,
    nazwa character varying(100) NOT NULL
);


ALTER TABLE public.kategorie OWNER TO postgres;

--
-- TOC entry 219 (class 1259 OID 17052)
-- Name: kategorie_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.kategorie_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.kategorie_id_seq OWNER TO postgres;

--
-- TOC entry 5056 (class 0 OID 0)
-- Dependencies: 219
-- Name: kategorie_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.kategorie_id_seq OWNED BY public.kategorie.id;


--
-- TOC entry 224 (class 1259 OID 17079)
-- Name: magazyn; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.magazyn (
    id integer NOT NULL,
    produkt_id integer NOT NULL,
    ilosc integer DEFAULT 0 NOT NULL,
    dostepnosc boolean DEFAULT false NOT NULL
);


ALTER TABLE public.magazyn OWNER TO postgres;

--
-- TOC entry 223 (class 1259 OID 17078)
-- Name: magazyn_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.magazyn_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.magazyn_id_seq OWNER TO postgres;

--
-- TOC entry 5057 (class 0 OID 0)
-- Dependencies: 223
-- Name: magazyn_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.magazyn_id_seq OWNED BY public.magazyn.id;


--
-- TOC entry 222 (class 1259 OID 17064)
-- Name: produkty; Type: TABLE; Schema: public; Owner: postgres
--

CREATE TABLE public.produkty (
    id integer NOT NULL,
    nazwa character varying(200) NOT NULL,
    kategoria_id integer NOT NULL
);


ALTER TABLE public.produkty OWNER TO postgres;

--
-- TOC entry 221 (class 1259 OID 17063)
-- Name: produkty_id_seq; Type: SEQUENCE; Schema: public; Owner: postgres
--

CREATE SEQUENCE public.produkty_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE public.produkty_id_seq OWNER TO postgres;

--
-- TOC entry 5058 (class 0 OID 0)
-- Dependencies: 221
-- Name: produkty_id_seq; Type: SEQUENCE OWNED BY; Schema: public; Owner: postgres
--

ALTER SEQUENCE public.produkty_id_seq OWNED BY public.produkty.id;


--
-- TOC entry 4876 (class 2604 OID 17102)
-- Name: cennik id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.cennik ALTER COLUMN id SET DEFAULT nextval('public.cennik_id_seq'::regclass);


--
-- TOC entry 4871 (class 2604 OID 17056)
-- Name: kategorie id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.kategorie ALTER COLUMN id SET DEFAULT nextval('public.kategorie_id_seq'::regclass);


--
-- TOC entry 4873 (class 2604 OID 17082)
-- Name: magazyn id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.magazyn ALTER COLUMN id SET DEFAULT nextval('public.magazyn_id_seq'::regclass);


--
-- TOC entry 4872 (class 2604 OID 17067)
-- Name: produkty id; Type: DEFAULT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.produkty ALTER COLUMN id SET DEFAULT nextval('public.produkty_id_seq'::regclass);


--
-- TOC entry 5049 (class 0 OID 17099)
-- Dependencies: 226
-- Data for Name: cennik; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.cennik (id, produkt_id, cena) FROM stdin;
1	1	4.50
2	2	3.20
3	3	350.00
4	4	4.50
5	5	3.20
6	6	45.00
7	7	350.00
\.


--
-- TOC entry 5043 (class 0 OID 17053)
-- Dependencies: 220
-- Data for Name: kategorie; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.kategorie (id, nazwa) FROM stdin;
1	Pieczywo
2	Nabiał
3	Odzież
4	Alkohol
\.


--
-- TOC entry 5047 (class 0 OID 17079)
-- Dependencies: 224
-- Data for Name: magazyn; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.magazyn (id, produkt_id, ilosc, dostepnosc) FROM stdin;
1	1	50	t
2	2	20	t
3	3	20	t
4	4	50	t
5	5	20	t
6	6	5	f
7	7	20	t
\.


--
-- TOC entry 5045 (class 0 OID 17064)
-- Dependencies: 222
-- Data for Name: produkty; Type: TABLE DATA; Schema: public; Owner: postgres
--

COPY public.produkty (id, nazwa, kategoria_id) FROM stdin;
1	Chleb	1
2	Mleko	2
3	Bluza	3
4	Chleb	1
5	Mleko	2
6	Wino	4
7	Bluza	3
\.


--
-- TOC entry 5059 (class 0 OID 0)
-- Dependencies: 225
-- Name: cennik_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.cennik_id_seq', 7, true);


--
-- TOC entry 5060 (class 0 OID 0)
-- Dependencies: 219
-- Name: kategorie_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.kategorie_id_seq', 4, true);


--
-- TOC entry 5061 (class 0 OID 0)
-- Dependencies: 223
-- Name: magazyn_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.magazyn_id_seq', 7, true);


--
-- TOC entry 5062 (class 0 OID 0)
-- Dependencies: 221
-- Name: produkty_id_seq; Type: SEQUENCE SET; Schema: public; Owner: postgres
--

SELECT pg_catalog.setval('public.produkty_id_seq', 7, true);


--
-- TOC entry 4889 (class 2606 OID 17108)
-- Name: cennik cennik_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.cennik
    ADD CONSTRAINT cennik_pkey PRIMARY KEY (id);


--
-- TOC entry 4891 (class 2606 OID 17110)
-- Name: cennik cennik_produkt_id_key; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.cennik
    ADD CONSTRAINT cennik_produkt_id_key UNIQUE (produkt_id);


--
-- TOC entry 4879 (class 2606 OID 17062)
-- Name: kategorie kategorie_nazwa_key; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.kategorie
    ADD CONSTRAINT kategorie_nazwa_key UNIQUE (nazwa);


--
-- TOC entry 4881 (class 2606 OID 17060)
-- Name: kategorie kategorie_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.kategorie
    ADD CONSTRAINT kategorie_pkey PRIMARY KEY (id);


--
-- TOC entry 4885 (class 2606 OID 17090)
-- Name: magazyn magazyn_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.magazyn
    ADD CONSTRAINT magazyn_pkey PRIMARY KEY (id);


--
-- TOC entry 4887 (class 2606 OID 17092)
-- Name: magazyn magazyn_produkt_id_key; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.magazyn
    ADD CONSTRAINT magazyn_produkt_id_key UNIQUE (produkt_id);


--
-- TOC entry 4883 (class 2606 OID 17072)
-- Name: produkty produkty_pkey; Type: CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.produkty
    ADD CONSTRAINT produkty_pkey PRIMARY KEY (id);


--
-- TOC entry 4894 (class 2606 OID 17111)
-- Name: cennik cennik_produkt_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.cennik
    ADD CONSTRAINT cennik_produkt_id_fkey FOREIGN KEY (produkt_id) REFERENCES public.produkty(id) ON DELETE CASCADE;


--
-- TOC entry 4893 (class 2606 OID 17093)
-- Name: magazyn magazyn_produkt_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.magazyn
    ADD CONSTRAINT magazyn_produkt_id_fkey FOREIGN KEY (produkt_id) REFERENCES public.produkty(id) ON DELETE CASCADE;


--
-- TOC entry 4892 (class 2606 OID 17073)
-- Name: produkty produkty_kategoria_id_fkey; Type: FK CONSTRAINT; Schema: public; Owner: postgres
--

ALTER TABLE ONLY public.produkty
    ADD CONSTRAINT produkty_kategoria_id_fkey FOREIGN KEY (kategoria_id) REFERENCES public.kategorie(id) ON DELETE CASCADE;


-- Completed on 2026-02-09 19:45:57

--
-- PostgreSQL database dump complete
--

\unrestrict e9GJRDZW2iNF03bnprs1eMvTxwfzrOLnlALALu1hfbR0kpxqdKv4db2hqeg3cLw

