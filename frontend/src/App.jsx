import React, { useEffect, useState } from "react";
import { api } from "./api";

function formatDateTimeLocal(value) {
  if (!value) return "";
  const date = new Date(value);
  const pad = (n) => n.toString().padStart(2, "0");
  const yyyy = date.getFullYear();
  const MM = pad(date.getMonth() + 1);
  const dd = pad(date.getDate());
  const hh = pad(date.getHours());
  const mm = pad(date.getMinutes());
  return `${yyyy}-${MM}-${dd}T${hh}:${mm}`;
}

function App() {
  const [activeTab, setActiveTab] = useState("auth");
  const [activeAuthView, setActiveAuthView] = useState("login"); // "login" | "register"

  const [registerEmail, setRegisterEmail] = useState("");
  const [registerPassword, setRegisterPassword] = useState("");
  const [loginEmail, setLoginEmail] = useState("");
  const [loginPassword, setLoginPassword] = useState("");
  const [isLoggedIn, setIsLoggedIn] = useState(false);

  const [salles, setSalles] = useState([]);
  const [sallesLoading, setSallesLoading] = useState(false);

  const [selectedSalleId, setSelectedSalleId] = useState("");
  const [dateDebut, setDateDebut] = useState("");
  const [dateFin, setDateFin] = useState("");

  const [reservations, setReservations] = useState([]);
  const [reservationsLoading, setReservationsLoading] = useState(false);

  const [message, setMessage] = useState("");
  const [error, setError] = useState("");

  useEffect(() => {
    setMessage("");
    setError("");
  }, [activeTab]);

  const handleRegister = async (e) => {
    e.preventDefault();
    setMessage("");
    setError("");
    try {
      await api.register(registerEmail, registerPassword);
      setMessage("Compte créé avec succès. Vous pouvez maintenant vous connecter.");
      setRegisterPassword("");
    } catch (err) {
      setError(err.message);
    }
  };

  const handleLogin = async (e) => {
    e.preventDefault();
    setMessage("");
    setError("");
    try {
      await api.login(loginEmail, loginPassword);
      setIsLoggedIn(true);
      setLoginPassword("");
      setMessage("Connexion réussie.");
    } catch (err) {
      setError(err.message);
    }
  };

  const handleLogout = async () => {
    setMessage("");
    setError("");
    try {
      await api.logout();
      setIsLoggedIn(false);
      setReservations([]);
    } catch (err) {
      setError(err.message);
    }
  };

  const loadSalles = async () => {
    setSallesLoading(true);
    setError("");
    try {
      const data = await api.getSalles();
      setSalles(data || []);
      if (!selectedSalleId && data && data.length > 0) {
        setSelectedSalleId(String(data[0].id));
      }
    } catch (err) {
      setError(err.message);
    } finally {
      setSallesLoading(false);
    }
  };

  const loadReservations = async () => {
    setReservationsLoading(true);
    setError("");
    try {
      const data = await api.getMesReservations();
      setReservations(data || []);
    } catch (err) {
      setError(err.message);
    } finally {
      setReservationsLoading(false);
    }
  };

  const handleCreateReservation = async (e) => {
    e.preventDefault();
    setMessage("");
    setError("");
    if (!isLoggedIn) {
      setError("Vous devez être connecté pour réserver une salle.");
      return;
    }
    if (!selectedSalleId || !dateDebut || !dateFin) {
      setError("Veuillez remplir tous les champs.");
      return;
    }
    try {
      const dateDebutValue = new Date(dateDebut);
      const dateFinValue = new Date(dateFin);
      const toBackend = (d) => d.toISOString().slice(0, 19);

      await api.createReservation(
        Number(selectedSalleId),
        toBackend(dateDebutValue),
        toBackend(dateFinValue)
      );
      setMessage("Réservation créée avec succès.");
      await loadReservations();
    } catch (err) {
      setError(err.message);
    }
  };

  useEffect(() => {
    if (activeTab === "salles") {
      loadSalles();
    }
    if (activeTab === "reservations") {
      loadReservations();
    }
  }, [activeTab]);

  return (
    <div className="app">
      <header className="header">
        <h1>EasyBooking</h1>
        <div className="header-right">
          {isLoggedIn ? (
            <>
              <span className="badge">Connecté</span>
              <button className="btn secondary" onClick={handleLogout}>
                Déconnexion
              </button>
            </>
          ) : (
            <span className="badge badge-muted">Non connecté</span>
          )}
        </div>
      </header>

      <nav className="tabs">
        <button
          className={`tab ${activeTab === "auth" ? "active" : ""}`}
          onClick={() => setActiveTab("auth")}
        >
          Connexion / Inscription
        </button>
        <button
          className={`tab ${activeTab === "salles" ? "active" : ""}`}
          onClick={() => setActiveTab("salles")}
        >
          Salles disponibles
        </button>
        <button
          className={`tab ${activeTab === "reservation" ? "active" : ""}`}
          onClick={() => setActiveTab("reservation")}
          disabled={!isLoggedIn}
        >
          Réserver une salle
        </button>
        <button
          className={`tab ${activeTab === "reservations" ? "active" : ""}`}
          onClick={() => setActiveTab("reservations")}
          disabled={!isLoggedIn}
        >
          Mes réservations
        </button>
      </nav>

      <main className="content">
        {message && <div className="alert success">{message}</div>}
        {error && <div className="alert error">{error}</div>}

        {activeTab === "auth" && (
          <section className="card auth-card">
            {activeAuthView === "login" ? (
              <>
                <h2>Connexion</h2>
                <form onSubmit={handleLogin} className="form">
                  <label>
                    Email
                    <input
                      type="email"
                      value={loginEmail}
                      onChange={(e) => setLoginEmail(e.target.value)}
                      required
                    />
                  </label>
                  <label>
                    Mot de passe
                    <input
                      type="password"
                      value={loginPassword}
                      onChange={(e) => setLoginPassword(e.target.value)}
                      required
                    />
                  </label>
                  <button className="btn primary" type="submit">
                    Se connecter
                  </button>
                </form>
                <p className="auth-switch">
                  Pas de compte ?{" "}
                  <button
                    type="button"
                    className="link-button"
                    onClick={() => setActiveAuthView("register")}
                  >
                    Créer un compte
                  </button>
                </p>
              </>
            ) : (
              <>
                <h2>Créer un compte</h2>
                <form onSubmit={handleRegister} className="form">
                  <label>
                    Email
                    <input
                      type="email"
                      value={registerEmail}
                      onChange={(e) => setRegisterEmail(e.target.value)}
                      required
                    />
                  </label>
                  <label>
                    Mot de passe
                    <input
                      type="password"
                      value={registerPassword}
                      onChange={(e) => setRegisterPassword(e.target.value)}
                      required
                    />
                  </label>
                  <button className="btn primary" type="submit">
                    S'inscrire
                  </button>
                </form>
                <p className="auth-switch">
                  Déjà un compte ?{" "}
                  <button
                    type="button"
                    className="link-button"
                    onClick={() => setActiveAuthView("login")}
                  >
                    Se connecter
                  </button>
                </p>
              </>
            )}
          </section>
        )}

        {activeTab === "salles" && (
          <section>
            <div className="section-header">
              <h2>Liste des salles disponibles</h2>
              <button className="btn secondary" onClick={loadSalles} disabled={sallesLoading}>
                Rafraîchir
              </button>
            </div>
            {sallesLoading ? (
              <p>Chargement des salles...</p>
            ) : salles.length === 0 ? (
              <p>Aucune salle trouvée.</p>
            ) : (
              <div className="list">
                {salles.map((salle) => (
                  <div key={salle.id} className="list-item">
                    <div>
                      <h3>{salle.nom || `Salle #${salle.id}`}</h3>
                      {salle.capacite && <p>Capacité: {salle.capacite}</p>}
                    </div>
                  </div>
                ))}
              </div>
            )}
          </section>
        )}

        {activeTab === "reservation" && (
          <section className="card">
            <h2>Réserver une salle</h2>
            {!isLoggedIn && (
              <p>Vous devez être connecté pour créer une réservation.</p>
            )}
            <form onSubmit={handleCreateReservation} className="form">
              <label>
                Salle
                <select
                  value={selectedSalleId}
                  onChange={(e) => setSelectedSalleId(e.target.value)}
                  onFocus={loadSalles}
                  required
                >
                  <option value="">Choisissez une salle</option>
                  {salles.map((salle) => (
                    <option key={salle.id} value={salle.id}>
                      {salle.nom || `Salle #${salle.id}`}
                    </option>
                  ))}
                </select>
              </label>
              <label>
                Date et heure de début
                <input
                  type="datetime-local"
                  value={formatDateTimeLocal(dateDebut)}
                  onChange={(e) => setDateDebut(e.target.value)}
                  required
                />
              </label>
              <label>
                Date et heure de fin
                <input
                  type="datetime-local"
                  value={formatDateTimeLocal(dateFin)}
                  onChange={(e) => setDateFin(e.target.value)}
                  required
                />
              </label>
              <button className="btn primary" type="submit">
                Réserver
              </button>
            </form>
          </section>
        )}

        {activeTab === "reservations" && (
          <section>
            <div className="section-header">
              <h2>Mes réservations</h2>
              <button
                className="btn secondary"
                onClick={loadReservations}
                disabled={reservationsLoading}
              >
                Rafraîchir
              </button>
            </div>
            {reservationsLoading ? (
              <p>Chargement des réservations...</p>
            ) : reservations.length === 0 ? (
              <p>Aucune réservation trouvée.</p>
            ) : (
              <div className="list">
                {reservations.map((r) => (
                  <div key={r.id} className="list-item">
                    <div>
                      <h3>Réservation #{r.id}</h3>
                      {r.salle && (
                        <p>
                          Salle: {r.salle.nom || `#${r.salle.id}`}{" "}
                          {r.salle.capacite && `(capacité ${r.salle.capacite})`}
                        </p>
                      )}
                      <p>
                        Début:{" "}
                        {r.dateDebut
                          ? new Date(r.dateDebut).toLocaleString()
                          : "N/A"}
                      </p>
                      <p>
                        Fin:{" "}
                        {r.dateFin
                          ? new Date(r.dateFin).toLocaleString()
                          : "N/A"}
                      </p>
                    </div>
                  </div>
                ))}
              </div>
            )}
          </section>
        )}
      </main>
    </div>
  );
}

export default App;

