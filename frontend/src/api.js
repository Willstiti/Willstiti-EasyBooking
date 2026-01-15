const API_BASE_URL = "http://localhost:8080";

async function request(path, options = {}) {
  const response = await fetch(`${API_BASE_URL}${path}`, {
    headers: {
      "Content-Type": "application/json",
      ...(options.headers || {})
    },
    credentials: "include", // important pour la session HttpSession
    ...options
  });

  const text = await response.text();
  let data;
  try {
    data = text ? JSON.parse(text) : null;
  } catch {
    data = text;
  }

  if (!response.ok) {
    const message = typeof data === "string" ? data : (data?.message || "Erreur inconnue");
    throw new Error(message);
  }

  return data;
}

export const api = {
  register: (email, password) =>
    request("/register", {
      method: "POST",
      body: JSON.stringify({ email, password })
    }),

  login: (email, password) =>
    request("/login", {
      method: "POST",
      body: JSON.stringify({ email, password })
    }),

  logout: () =>
    request("/logout", {
      method: "POST"
    }),

  getSalles: () => request("/salles"),

  createReservation: (salleId, dateDebut, dateFin) =>
    request("/reservations", {
      method: "POST",
      body: JSON.stringify({ salleId, dateDebut, dateFin })
    }),

  getMesReservations: () => request("/reservations"),

  deleteReservation: (id) =>
    request(`/reservations/${id}`, {
      method: "DELETE"
    }),

  getReservationsForSalleAndDate: (salleId, date) =>
    request(`/reservations/salle/${salleId}?date=${encodeURIComponent(date)}`)
};

