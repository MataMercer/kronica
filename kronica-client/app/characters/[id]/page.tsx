import { fetchCharacter } from "@/app/fetch/characters";
import Image from "next/image";
import CharacterProfilePicture from "./CharacterProfilePicture";
import {DropdownMenu, DropdownMenuContent, DropdownMenuItem, DropdownMenuTrigger} from "@/components/ui/dropdown-menu";
import {EllipsisVertical, Flag, PencilIcon, Trash} from "lucide-react";
import AuthProtection from "@/app/auth/AuthProtection";
import Link from "next/link";

export default async function CharacterPage(props: {
    params: Promise<{ id: string }>;
}) {
    const params = await props.params;
    const id = params.id;
    const character = id && (await fetchCharacter(id));

    console.log(character)
    return (
        character && (
            <div className="">
                <h1 className="text-3xl flex justify-left">{character.name}</h1>
                <DropdownMenu>
                    <DropdownMenuTrigger className="p-0 ">
                        <EllipsisVertical size={20}/>
                    </DropdownMenuTrigger>
                    <DropdownMenuContent>
                        <AuthProtection
                            requiredRole={"AUTHENTICATED_USER"}
                            requiredOwnerId={character.author.id}
                        >
                            <DropdownMenuItem>
                                <Link
                                    href={`/characters/${character.id}/edit`}
                                    className="flex items-center space-x-1"
                                >
                                    <PencilIcon/>
                                    <span>EDIT</span>
                                </Link>
                            </DropdownMenuItem>
                            <DropdownMenuItem
                                // onClick={() => onDelete(timeline.id)}
                            >
                                <Trash/>
                                <span>DELETE</span>
                            </DropdownMenuItem>
                        </AuthProtection>

                        <DropdownMenuItem>
                            <Flag/>
                            <span>REPORT</span>
                        </DropdownMenuItem>
                    </DropdownMenuContent>
                </DropdownMenu>
                <div className="m-2  p-2 border-black border-2 float-right">
                    <div className="border-black border-2 flex justify-center text-2xl font-bold">
                        {character.name}
                    </div>
                    <CharacterProfilePicture
                        profilePictures={character.profilePictures}
                    />

                    <div className="">
                        <DataRow infoKey="Name" infoValue={character.name} />

                        {character.traits &&
                            character.traits.map(
                                (it, index) => (
                                    <DataRow
                                        key={index}
                                        infoKey={it.name}
                                        infoValue={it.value}
                                    />
                                )
                            )}
                    </div>
                </div>
                <div className="m-2">{character.body}</div>
            </div>
        )
    );
}

type DataRowProps = {
    infoKey: string;
    infoValue: string;
};

function DataRow({ infoKey, infoValue }: DataRowProps) {
    return (
        infoValue && (
            <div className="grid grid-cols-2">
                <div className="font-bold">{infoKey}</div>
                <div className="">{infoValue}</div>
            </div>
        )
    );
}
