import { User, UserRole } from "../Types/Models";

export function hasRequiredRole(
  user: User | undefined,
  requiredRole: keyof typeof UserRole
): boolean {
  if (!user) {
    return false;
  }
  const userRoleLevel = UserRole[user.role];
  const requiredRoleLevel = UserRole[requiredRole];
  return userRoleLevel >= requiredRoleLevel;
}