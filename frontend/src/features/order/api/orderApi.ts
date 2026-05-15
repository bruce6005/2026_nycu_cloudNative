import { CONFIG } from "../../../config/config";

export const getOrders = async () => {
  const res = await fetch(`${CONFIG.API_BASE}/orders`);
  return res.json();
};
