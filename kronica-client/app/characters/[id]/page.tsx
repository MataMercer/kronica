import { fetchCharacter } from "@/app/fetch/characters";
import Image from "next/image";
import CharacterProfilePicture from "./CharacterProfilePicture";

export default async function CharacterPage(props: {
    params: Promise<{ id: string }>;
}) {
    const params = await props.params;
    const id = params.id;
    const character = id && (await fetchCharacter(id));

    return (
        character && (
            <div className="">
                <h1 className="text-3xl flex justify-left">{character.name}</h1>
                <div className="m-2  p-2 border-black border-2 float-right">
                    <div className="border-black border-2 flex justify-center text-2xl font-bold">
                        {character.name}
                    </div>
                    <CharacterProfilePicture
                        profilePictures={character.profilePictures}
                    />

                    <div className="">
                        <DataRow infoKey="Name" infoValue={character.name} />
                        <DataRow
                            infoKey="Status"
                            infoValue={character.status}
                        />
                        <DataRow
                            infoKey="First Seen"
                            infoValue={character.gender}
                        />
                        <DataRow
                            infoKey="Age"
                            infoValue={character.age.toString()}
                        />
                        <DataRow
                            infoKey="Birthday"
                            infoValue={character.birthday}
                        />
                        <DataRow
                            infoKey="First Seen"
                            infoValue={character.firstSeen}
                        />
                        {character.traits &&
                            Object.entries(character.traits).map(
                                (it, index) => (
                                    <DataRow
                                        key={index}
                                        infoKey={it[0]}
                                        infoValue={it[1] as string}
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
