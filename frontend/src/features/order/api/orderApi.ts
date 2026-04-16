const API_BASE = "http://localhost:8080";

export const getOrders = async () => {
  const res = await fetch(`${API_BASE}/orders`);
  return res.json();
};