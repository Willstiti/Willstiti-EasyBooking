import React, { useEffect, useState } from "react";
import { api } from "./api";

function App() {
  const [view, setView] = useState("login"); // "login" | "register" | "app"
  const [activeTab, setActiveTab] = useState("salles");

  const [registerEmail, setRegisterEmail] = useState("");
  const [registerPassword, setRegisterPassword] = useState("");
  const [loginEmail, setLoginEmail] = useState("");
  const [loginPassword, setLoginPassword] = useState("");
  const [isLoggedIn, setIsLoggedIn] = useState(false);

  const [salles, setSalles] = useState([]);
  const [sallesLoading, setSallesLoading] = useState(false);

  const [selectedSalleId, setSelectedSalleId] = useState("");
  const [reservationDate, setReservationDate] = useState("");
  const [availableSlots, setAvailableSlots] = useState([]);
  const [slotsLoading, setSlotsLoading] = useState(false);
  const [selectedStartSlotIndex, setSelectedStartSlotIndex] = useState(null);
  const [selectedEndSlotIndex, setSelectedEndSlotIndex] = useState(null);

  const [reservations, setReservations] = useState([]);
  const [reservationsLoading, setReservationsLoading] = useState(false);

  const [message, setMessage] = useState("");
  const [error, setError] = useState("");

  useEffect(() => {
    setMessage("");
    setError("");
  }, [activeTab, view]);

  const handleRegister = async (e) => {
    e.preventDefault();
    setMessage("");
    setError("");
    try {
      await api.register(registerEmail, registerPassword);
      setMessage("Compte créé avec succès. Vous pouvez maintenant vous connecter.");
      setRegisterPassword("");
      setView("login");
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
      setView("app");
      setActiveTab("salles");
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
      setView("login");
      setActiveTab("salles");
    } catch (err) {
      setError(err.message);
    }
  };

  const loadSalles = async () => {
    setSallesLoading(true);
    setError("");
    try {
      const data = await api.getSalles();
      console.log("API /salles response:", data);
      // On s'assure que salles est toujours un tableau
      const list = Array.isArray(data) ? data : [];
      setSalles(list);
      if (!selectedSalleId && list.length > 0) {
        setSelectedSalleId(String(list[0].id));
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
    // Conservée pour compatibilité mais non utilisée directement
    e?.preventDefault?.();
  };

  const loadSlots = async () => {
    if (!selectedSalleId || !reservationDate) {
      setAvailableSlots([]);
      return;
    }
    setSlotsLoading(true);
    setError("");
    try {
      const reservationsSalle = await api.getReservationsForSalleAndDate(
        Number(selectedSalleId),
        reservationDate
      );

      const openingHour = 8;
      const closingHour = 18;
      const slotDurationMinutes = 60;

      const bookedIntervals = (reservationsSalle || []).map((r) => ({
        start: new Date(r.dateDebut),
        end: new Date(r.dateFin)
      }));

      const slots = [];
      const [year, month, day] = reservationDate.split("-").map((v) => Number(v));

      const makeDate = (h, m) => new Date(year, month - 1, day, h, m, 0);

      for (
        let minutes = openingHour * 60;
        minutes + slotDurationMinutes <= closingHour * 60;
        minutes += slotDurationMinutes
      ) {
        const startHour = Math.floor(minutes / 60);
        const startMinute = minutes % 60;
        const endMinutes = minutes + slotDurationMinutes;
        const endHour = Math.floor(endMinutes / 60);
        const endMinute = endMinutes % 60;

        const startDate = makeDate(startHour, startMinute);
        const endDate = makeDate(endHour, endMinute);

        const overlaps = bookedIntervals.some(
          (b) => startDate < b.end && endDate > b.start
        );

        if (!overlaps) {
          const pad = (n) => String(n).padStart(2, "0");
          slots.push({
            label: `${pad(startHour)}:${pad(startMinute)} - ${pad(endHour)}:${pad(endMinute)}`,
            startDate,
            endDate
          });
        }
      }

      setAvailableSlots(slots);
      setSelectedStartSlotIndex(null);
      setSelectedEndSlotIndex(null);
    } catch (err) {
      setError(err.message);
      setAvailableSlots([]);
    } finally {
      setSlotsLoading(false);
    }
  };

  const handleConfirmRange = async () => {
    if (!isLoggedIn) {
      setError("Vous devez être connecté pour réserver une salle.");
      return;
    }
    if (!selectedSalleId || !reservationDate) {
      setError("Veuillez choisir une salle et une date.");
      return;
    }
    if (
      selectedStartSlotIndex === null ||
      selectedEndSlotIndex === null ||
      availableSlots.length === 0
    ) {
      setError("Veuillez choisir un créneau de début et un créneau de fin.");
      return;
    }

    const startIndex = Math.min(selectedStartSlotIndex, selectedEndSlotIndex);
    const endIndex = Math.max(selectedStartSlotIndex, selectedEndSlotIndex);

    const startSlot = availableSlots[startIndex];
    const endSlot = availableSlots[endIndex];

    setMessage("");
    setError("");
    try {
      const toBackend = (d) => d.toISOString().slice(0, 19);
      await api.createReservation(
        Number(selectedSalleId),
        toBackend(startSlot.startDate),
        toBackend(endSlot.endDate)
      );
      setMessage("Réservation créée avec succès.");
      await loadReservations();
      await loadSlots();
      setSelectedStartSlotIndex(null);
      setSelectedEndSlotIndex(null);
    } catch (err) {
      setError(err.message);
    }
  };

  const handleDeleteReservation = async (id) => {
    setMessage("");
    setError("");
    try {
      await api.deleteReservation(id);
      setMessage("Réservation supprimée avec succès.");
      await loadReservations();
    } catch (err) {
      setError(err.message);
    }
  };

  useEffect(() => {
    if (view !== "app") {
      return;
    }
    if (activeTab === "salles") {
      loadSalles();
    }
    if (activeTab === "reservations") {
      loadReservations();
    }
    if (activeTab === "reservation" && selectedSalleId && reservationDate) {
      loadSlots();
    }
  }, [view, activeTab, selectedSalleId, reservationDate]);

  const renderAuth = () => (
    <main className="content">
      {message && <div className="alert success">{message}</div>}
      {error && <div className="alert error">{error}</div>}

      {view === "login" && (
        <section className="card auth-card">
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
              onClick={() => setView("register")}
            >
              Créer un compte
            </button>
          </p>
        </section>
      )}

      {view === "register" && (
        <section className="card auth-card">
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
              onClick={() => setView("login")}
            >
              Se connecter
            </button>
          </p>
        </section>
      )}
    </main>
  );

  const renderApp = () => (
    <>
      <nav className="tabs">
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
                    <div className="actions">
                      <button
                        className="btn primary"
                        onClick={() => {
                          setSelectedSalleId(String(salle.id));
                          setActiveTab("reservation");
                        }}
                      >
                        Voir les créneaux
                      </button>
                    </div>
                  </div>
                ))}
              </div>
            )}
          </section>
        )}

        {activeTab === "reservation" && (
          <section>
            <h2>Réserver une salle</h2>
            {!isLoggedIn && (
              <p>Vous devez être connecté pour créer une réservation.</p>
            )}
            <form className="form simple-form">
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
                Date
                <input
                  type="date"
                  value={reservationDate}
                  onChange={(e) => setReservationDate(e.target.value)}
                  required
                />
              </label>
            </form>
            <div style={{ marginTop: "8px" }}>
              {slotsLoading ? (
                <p>Chargement des créneaux disponibles...</p>
              ) : reservationDate && selectedSalleId ? (
                availableSlots.length === 0 ? (
                  <p>Aucun créneau disponible pour cette date.</p>
                ) : (
                  <>
                    <p style={{ fontSize: "0.85rem", marginBottom: "4px" }}>
                      Créneau de début :
                    </p>
                    <div
                      style={{
                        display: "flex",
                        flexWrap: "wrap",
                        gap: "4px"
                      }}
                    >
                      {availableSlots.map((slot, index) => (
                        <button
                          key={`start-${slot.label}`}
                          className={`btn secondary${
                            selectedStartSlotIndex === index ? " slot-selected" : ""
                          }`}
                          type="button"
                          onClick={() => setSelectedStartSlotIndex(index)}
                        >
                          {slot.label}
                        </button>
                      ))}
                    </div>
                    <p
                      style={{
                        fontSize: "0.85rem",
                        marginTop: "8px",
                        marginBottom: "4px"
                      }}
                    >
                      Créneau de fin :
                    </p>
                    <div
                      style={{
                        display: "flex",
                        flexWrap: "wrap",
                        gap: "4px"
                      }}
                    >
                      {availableSlots.map((slot, index) => (
                        <button
                          key={`end-${slot.label}`}
                          className={`btn secondary${
                            selectedEndSlotIndex === index ? " slot-selected" : ""
                          }`}
                          type="button"
                          onClick={() => setSelectedEndSlotIndex(index)}
                        >
                          {slot.label}
                        </button>
                      ))}
                    </div>
                    <div style={{ marginTop: "8px" }}>
                      <button
                        className="btn primary"
                        type="button"
                        onClick={handleConfirmRange}
                      >
                        Réserver ce créneau
                      </button>
                    </div>
                  </>
                )
              ) : (
                <p>Sélectionnez une salle et une date pour voir les créneaux.</p>
              )}
            </div>
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
                    <div className="actions">
                      <button
                        className="btn danger"
                        onClick={() => handleDeleteReservation(r.id)}
                      >
                        Supprimer
                      </button>
                    </div>
                  </div>
                ))}
              </div>
            )}
          </section>
        )}
      </main>
    </>
  );

  return (
    <div className="app">
      <header className="header">
        <h1>EasyBooking</h1>
        <div className="header-right">
          {isLoggedIn && view === "app" ? (
            <>
              <span className="badge">Connecté</span>
              <button className="btn secondary" onClick={handleLogout}>
                Déconnexion
              </button>
            </>
          ) : (
            <span className="badge badge-muted">
              {view === "register" ? "Inscription" : "Connexion requise"}
            </span>
          )}
        </div>
      </header>

      {view === "app" ? renderApp() : renderAuth()}
    </div>
  );
}

export default App;

