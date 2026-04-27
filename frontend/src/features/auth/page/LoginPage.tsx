import { GoogleLogin } from "@react-oauth/google";
import type { AuthUser } from "../model/AuthUser";
import { loginWithGoogle } from "../api/authApi";

type Props = {
  setUser: (user: AuthUser) => void;
};

export default function LoginPage({ setUser }: Props) {
  const handleLoginSuccess = async (credentialResponse: any) => {
    try {
      const credential = credentialResponse.credential;

      if (!credential) {
        throw new Error("Missing Google credential");
      }

      const user = await loginWithGoogle(credential);
      setUser(user);
    } catch (err) {
      console.error("Login failed", err);
    }
  };

  return (
    <div
      style={{
        display: "flex",
        flexDirection: "column",
        alignItems: "center",
        justifyContent: "center",
        height: "100vh",
        background: "#f0f2f5",
      }}
    >
      <h1 style={{ marginBottom: "10px" }}>Welcome to LAB SYSTEM</h1>
      <p style={{ marginBottom: "20px", color: "#555" }}>
        Please sign in to continue
      </p>

      <GoogleLogin
        onSuccess={handleLoginSuccess}
        onError={() => console.error("Login Failed")}
      />
    </div>
  );
}