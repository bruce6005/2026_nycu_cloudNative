import { useEffect, useState } from "react";
import type { AuthUser, ManagerOption, UserRole } from "../model/AuthUser";
import { fetchManagerOptions, setupUserProfile } from "../api/authApi";

type Props = {
  user: AuthUser;
  setUser: (user: AuthUser) => void;
};

export default function ProfileSetupPage({ user, setUser }: Props) {
  const [role, setRole] = useState<UserRole>("REQUESTER");
  const [managerId, setManagerId] = useState("");
  const [managerOptions, setManagerOptions] = useState<ManagerOption[]>([]);
  const [isLoadingManagers, setIsLoadingManagers] = useState(false);
  const [errorMessage, setErrorMessage] = useState("");

  useEffect(() => {
    if (role !== "REQUESTER") {
      setManagerId("");
      setManagerOptions([]);
      return;
    }

    let isCurrent = true;

    const loadManagers = async () => {
      setIsLoadingManagers(true);
      setErrorMessage("");

      try {
        const managers = await fetchManagerOptions();

        if (!isCurrent) {
          return;
        }

        setManagerOptions(managers);

        if (managers.length > 0) {
          setManagerId(String(managers[0].id));
        }
      } catch (err: any) {
        if (!isCurrent) {
          return;
        }

        const message =
          err.response?.data?.message ||
          err.response?.data?.error ||
          "Unable to load manager list";

        setErrorMessage(message);
      } finally {
        if (isCurrent) {
          setIsLoadingManagers(false);
        }
      }
    };

    loadManagers();

    return () => {
      isCurrent = false;
    };
  }, [role]);

  const handleSubmit = async () => {
    setErrorMessage("");

    const parsedManagerId = role === "REQUESTER" ? Number(managerId) : null;

    if (role === "REQUESTER" && !parsedManagerId) {
      setErrorMessage("Please select a manager");
      return;
    }

    try {
      const updatedUser = await setupUserProfile({
        userId: user.id,
        role,
        managerId: parsedManagerId,
      });

      setUser({ ...updatedUser, token: user.token });
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
          Manager{" "}
          <select
            value={managerId}
            onChange={(e) => setManagerId(e.target.value)}
            disabled={isLoadingManagers || managerOptions.length === 0}
          >
            {managerOptions.length === 0 && (
              <option value="">
                {isLoadingManagers ? "Loading managers..." : "No managers available"}
              </option>
            )}
            {managerOptions.map((manager) => (
              <option key={manager.id} value={manager.id}>
                {manager.name || manager.email} ({manager.email})
              </option>
            ))}
          </select>
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
