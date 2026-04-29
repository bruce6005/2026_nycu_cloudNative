const API_BASE = "http://localhost:8080/api/request";

export const getRequest = async () => {
  const res = await fetch(API_BASE);
  return res.json();
};

export const createRequest = async (data: {
  title: string;
  factoryUserId: number;
  priority: number;
  description: string;
}) => {
  const res = await fetch(API_BASE, {
    method: "POST",
    headers: { "Content-Type": "application/json" },
    body: JSON.stringify(data),
  });
  return res.json();
};

export const getRequestById = async (id: number) => {
  const res = await fetch(`${API_BASE}/${id}`);
  return res.json();
};
