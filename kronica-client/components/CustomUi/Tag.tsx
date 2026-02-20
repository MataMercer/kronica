import { Tag } from "lucide-react";

type Props = {
    children?: React.ReactNode;
};

export default function TagComp({ children }: Props) {
    return (
        <span className="   pt-1 pb-1 px-2  text-base">
            <div className="flex capitalize ">
                <Tag className="mx-1" />
                {children}
            </div>
        </span>
    );
}
