import { useState } from "react";
import type { AuthUser, UserRole } from "../model/AuthUser";
import { setupUserProfile } from "../api/authApi";

type Props = {
  user: AuthUser;
  setUser: (user: AuthUser) => void;
};

export default function ProfileSetupPage({ user, setUser }: Props) {
  const [role, setRole] = useState<UserRole>("REQUESTER");
  const [managerId, setManagerId] = useState("");
  const [errorMessage, setErrorMessage] = useState("");

  const handleSubmit = async () => {
    setErrorMessage("");

    const parsedManagerId = role === "REQUESTER" ? Number(managerId) : null;

    if (role === "REQUESTER" && !parsedManagerId) {
      setErrorMessage("Please enter manager ID");
      return;
    }

    try {
      const updatedUser = await setupUserProfile({
        userId: user.id,
        role,
        managerId: parsedManagerId,
      });

      setUser(updatedUser);
    } catch (err: any) {
      const message =
        err.response?.data?.message ||
        err.response?.data?.error ||
        "Setup profile failed";

      setErrorMessage(message);
      console.error("Setup profile failed: ", err);
      console.error("Backend error:", err.response?.data);
    }
  };

  return (
    <div
      style={{
        display: "flex",
        flexDirection: "column",
        gap: "12px",
        alignItems: "center",
        justifyContent: "center",
        height: "100vh",
        background: "#f0f2f5",
      }}
    >
      <h1>Complete Your Profile</h1>
      <p>{user.email}</p>

      <label>
        Role{" "}
        <select value={role} onChange={(e) => setRole(e.target.value as UserRole)}>
          <option value="REQUESTER">Factory User</option>
          <option value="LAB_STAFF">Lab Staff</option>
          <option value="MANAGER">Lab Manager</option>
        </select>
      </label>

      {role === "REQUESTER" && (
        <label>
          Manager ID{" "}
          <input
            value={managerId}
            onChange={(e) => setManagerId(e.target.value)}
            placeholder="Enter approver user ID"
          />
        </label>
      )}

      {errorMessage && (
        <div style={{ color: "red", fontSize: "14px" }}>
          {errorMessage}
        </div>
      )}

      <button onClick={handleSubmit}>Save</button>
    </div>
  );
}