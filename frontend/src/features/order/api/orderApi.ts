import { authFetch } from "../../utils/apiClient";

export const getOrders = async () => {
  const res = await authFetch("/orders");
  return res.json();
};
